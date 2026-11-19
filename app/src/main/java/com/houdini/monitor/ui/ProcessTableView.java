package com.houdini.monitor.ui;

import com.houdini.monitor.domain.ProcessInfo;
import com.houdini.monitor.domain.ProcessQuery;
import com.houdini.monitor.i18n.Messages;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public final class ProcessTableView extends BorderPane {

    private final Messages messages;
    private final TableView<ProcessInfo> table = new TableView<>();
    private final Button endButton = new Button();
    private final TextField searchField = new TextField();
    private final Label emptyLabel = new Label();
    private final TableColumn<ProcessInfo, String> nameColumn = new TableColumn<>();
    private final TableColumn<ProcessInfo, Number> pidColumn = new TableColumn<>();
    private final TableColumn<ProcessInfo, Number> cpuColumn = new TableColumn<>();
    private final TableColumn<ProcessInfo, String> ramColumn = new TableColumn<>();
    private final TableColumn<ProcessInfo, String> userColumn = new TableColumn<>();

    private List<ProcessInfo> sourceProcesses = List.of();
    private ProcessQuery.SortColumn sortColumn = ProcessQuery.SortColumn.CPU;
    private boolean ascending;

    public ProcessTableView(Messages messages, Consumer<ProcessInfo> onEndProcess) {
        this.messages = messages;
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        pidColumn.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().pid()));
        cpuColumn.setCellValueFactory(data -> new SimpleDoubleProperty(round1(data.getValue().cpuPercent())));
        ramColumn.setCellValueFactory(data -> new SimpleStringProperty(formatBytes(data.getValue().memoryBytes())));
        userColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().user().isBlank() ? "?" : data.getValue().user()));

        wireHeaderSort(nameColumn, ProcessQuery.SortColumn.NAME);
        wireHeaderSort(pidColumn, ProcessQuery.SortColumn.PID);
        wireHeaderSort(cpuColumn, ProcessQuery.SortColumn.CPU);
        wireHeaderSort(ramColumn, ProcessQuery.SortColumn.RAM);
        wireHeaderSort(userColumn, ProcessQuery.SortColumn.USER);

        table.getColumns().addAll(List.of(nameColumn, pidColumn, cpuColumn, ramColumn, userColumn));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(emptyLabel);

        endButton.setDisable(true);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) ->
                endButton.setDisable(selected == null));
        endButton.setOnAction(e -> {
            ProcessInfo selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onEndProcess.accept(selected);
            }
        });

        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, value) -> refreshView());

        HBox searchBar = new HBox(8, searchField);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBar.setPadding(new Insets(8, 8, 4, 8));

        HBox actions = new HBox(8, endButton);
        actions.setPadding(new Insets(8));

        VBox center = new VBox(searchBar, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        setCenter(center);
        setBottom(actions);
        relocalize();
        refreshSortIndicators();
    }

    private void wireHeaderSort(TableColumn<ProcessInfo, ?> column, ProcessQuery.SortColumn target) {
        column.setSortable(false);
        column.setReorderable(false);
        Label header = new Label();
        header.setMaxWidth(Double.MAX_VALUE);
        header.setOnMouseClicked(e -> toggleSort(target));
        column.setGraphic(header);
        header.setText(decorateHeader(target, baseHeader(target)));
    }

    private void toggleSort(ProcessQuery.SortColumn column) {
        if (sortColumn == column) {
            ascending = !ascending;
        } else {
            sortColumn = column;
            ascending = column == ProcessQuery.SortColumn.NAME || column == ProcessQuery.SortColumn.USER;
        }
        refreshSortIndicators();
        refreshView();
    }

    private void refreshSortIndicators() {
        applyHeader(nameColumn, ProcessQuery.SortColumn.NAME);
        applyHeader(pidColumn, ProcessQuery.SortColumn.PID);
        applyHeader(cpuColumn, ProcessQuery.SortColumn.CPU);
        applyHeader(ramColumn, ProcessQuery.SortColumn.RAM);
        applyHeader(userColumn, ProcessQuery.SortColumn.USER);
    }

    private void applyHeader(TableColumn<?, ?> column, ProcessQuery.SortColumn target) {
        if (column.getGraphic() instanceof Label label) {
            label.setText(decorateHeader(target, baseHeader(target)));
        }
    }

    private String decorateHeader(ProcessQuery.SortColumn target, String base) {
        if (sortColumn != target) {
            return base;
        }
        return base + (ascending ? " ▲" : " ▼");
    }

    private String baseHeader(ProcessQuery.SortColumn column) {
        return switch (column) {
            case NAME -> messages.get("processes.column.name");
            case PID -> messages.get("processes.column.pid");
            case CPU -> messages.get("processes.column.cpu");
            case RAM -> messages.get("processes.column.ram");
            case USER -> messages.get("processes.column.user");
        };
    }

    public void updateProcesses(List<ProcessInfo> processes) {
        this.sourceProcesses = processes == null ? List.of() : List.copyOf(processes);
        refreshView();
    }

    private void refreshView() {
        ProcessInfo selected = table.getSelectionModel().getSelectedItem();
        List<ProcessInfo> filtered = ProcessQuery.filterAndSort(
                sourceProcesses,
                searchField.getText(),
                sortColumn,
                ascending
        );
        table.setItems(FXCollections.observableArrayList(filtered));
        if (selected != null) {
            filtered.stream()
                    .filter(p -> p.pid() == selected.pid())
                    .findFirst()
                    .ifPresent(p -> table.getSelectionModel().select(p));
        }
    }

    public void focusSearch() {
        searchField.requestFocus();
        searchField.selectAll();
    }

    public void relocalize() {
        searchField.setPromptText(messages.get("processes.search"));
        emptyLabel.setText(messages.get("processes.empty"));
        endButton.setText(messages.get("processes.end"));
        refreshSortIndicators();
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format("%.1f MB", mb);
        }
        return String.format("%.2f GB", mb / 1024.0);
    }
}
