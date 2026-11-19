package com.houdini.monitor.ui;

import com.houdini.monitor.domain.ResourceKind;
import com.houdini.monitor.domain.SystemMetricsSnapshot;
import com.houdini.monitor.i18n.Messages;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class ResourceSidebar extends VBox {

    public record SidebarItem(ResourceKind kind, String title, String subtitle) {
    }

    private final ListView<SidebarItem> listView = new ListView<>();
    private final Messages messages;
    private double cpu;
    private double ram;
    private double disk;
    private String networkSubtitle = "—";
    private String gpuSubtitle = "—";

    public ResourceSidebar(Messages messages, Consumer<ResourceKind> onSelect) {
        this.messages = messages;
        getStyleClass().add("sidebar-pane");
        setPadding(new Insets(8));
        setSpacing(6);
        setPrefWidth(220);
        listView.getStyleClass().add("sidebar-list");
        listView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(SidebarItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("sidebar-item-active");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.title() + "\n" + item.subtitle());
                    if (isSelected()) {
                        getStyleClass().add("sidebar-item-active");
                    }
                }
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                onSelect.accept(selected.kind());
            }
            listView.refresh();
        });
        VBox.setVgrow(listView, Priority.ALWAYS);
        getChildren().add(listView);
        refreshLabels();
    }

    public void selectDefault() {
        if (listView.getSelectionModel().getSelectedIndex() < 0 && !listView.getItems().isEmpty()) {
            listView.getSelectionModel().selectFirst();
        }
    }

    public void updateMetrics(SystemMetricsSnapshot snapshot) {
        cpu = snapshot.cpu().usagePercent();
        ram = snapshot.ram().usagePercent();
        disk = snapshot.disk().usagePercent();
        networkSubtitle = formatRate(snapshot.networkBytesPerSecIn() + snapshot.networkBytesPerSecOut());
        if (snapshot.gpu().available()) {
            gpuSubtitle = snapshot.gpu().usagePercent()
                    .map(ResourceSidebar::format)
                    .orElse(snapshot.gpu().name().isBlank() ? "OK" : snapshot.gpu().name());
        } else {
            gpuSubtitle = messages.get("unavailable");
        }
        int index = listView.getSelectionModel().getSelectedIndex();
        refreshLabels();
        if (index >= 0) {
            listView.getSelectionModel().select(index);
        }
    }

    public void refreshLabels() {
        ResourceKind selected = listView.getSelectionModel().getSelectedItem() == null
                ? ResourceKind.CPU
                : listView.getSelectionModel().getSelectedItem().kind();
        listView.getItems().setAll(
                new SidebarItem(ResourceKind.CPU, messages.get("sidebar.cpu"), format(cpu)),
                new SidebarItem(ResourceKind.RAM, messages.get("sidebar.ram"), format(ram)),
                new SidebarItem(ResourceKind.DISK, messages.get("sidebar.disk"), format(disk)),
                new SidebarItem(ResourceKind.NETWORK, messages.get("sidebar.network"), networkSubtitle),
                new SidebarItem(ResourceKind.GPU, messages.get("sidebar.gpu"), gpuSubtitle),
                new SidebarItem(ResourceKind.PROCESSES, messages.get("sidebar.processes"), "")
        );
        for (int i = 0; i < listView.getItems().size(); i++) {
            if (listView.getItems().get(i).kind() == selected) {
                listView.getSelectionModel().select(i);
                break;
            }
        }
    }

    private static String format(double value) {
        return String.format("%.1f %%", value);
    }

    static String formatRate(double bytesPerSec) {
        if (bytesPerSec < 1024) {
            return String.format("%.0f B/s", bytesPerSec);
        }
        double kb = bytesPerSec / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB/s", kb);
        }
        return String.format("%.2f MB/s", kb / 1024.0);
    }
}
