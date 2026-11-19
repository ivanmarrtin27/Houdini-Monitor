package com.houdini.monitor.ui;

import com.houdini.monitor.domain.CpuCoreSnapshot;
import com.houdini.monitor.domain.GpuSnapshot;
import com.houdini.monitor.domain.MetricHistory;
import com.houdini.monitor.domain.NetworkInterfaceSnapshot;
import com.houdini.monitor.domain.ResourceKind;
import com.houdini.monitor.domain.ResourceSnapshot;
import com.houdini.monitor.domain.SystemMetricsSnapshot;
import com.houdini.monitor.i18n.Messages;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class ResourceDetailPane extends BorderPane {

    private final Messages messages;
    private final Label usageLabel = new Label();
    private final Label secondaryLabel = new Label();
    private final Label listTitle = new Label();
    private final ListView<String> detailList = new ListView<>();
    private final HistoryChartView chartView = new HistoryChartView();
    private final VBox resourceBox = new VBox(10);
    private ResourceKind currentKind = ResourceKind.CPU;

    public ResourceDetailPane(Messages messages) {
        this.messages = messages;
        getStyleClass().add("detail-pane");
        setPadding(new Insets(12));
        resourceBox.setPadding(new Insets(4));
        VBox.setVgrow(chartView, Priority.ALWAYS);
        detailList.setPrefHeight(140);
        resourceBox.getChildren().addAll(usageLabel, secondaryLabel, chartView, listTitle, detailList);
        setCenter(resourceBox);
        relocalize();
        show(ResourceKind.CPU);
    }

    public void show(ResourceKind kind) {
        this.currentKind = kind;
        updateListTitle();
    }

    public void update(SystemMetricsSnapshot snapshot, MetricHistory history) {
        if (currentKind == ResourceKind.PROCESSES) {
            return;
        }
        switch (currentKind) {
            case CPU -> updateCpu(snapshot, history);
            case RAM -> updateRam(snapshot, history);
            case DISK -> updateDisk(snapshot, history);
            case NETWORK -> updateNetwork(snapshot, history);
            case GPU -> updateGpu(snapshot, history);
            default -> updateRam(snapshot, history);
        }
    }

    private void updateCpu(SystemMetricsSnapshot snapshot, MetricHistory history) {
        usageLabel.setText(messages.format("detail.usage", String.format("%.1f", snapshot.cpu().usagePercent())));
        setSecondaryVisible(false);
        setChartVisible(true);
        chartView.update(history);
        setListVisible(true);
        detailList.getItems().clear();
        if (snapshot.cpuCores().isEmpty()) {
            detailList.getItems().add(messages.get("unavailable"));
        } else {
            for (CpuCoreSnapshot core : snapshot.cpuCores()) {
                detailList.getItems().add(messages.format(
                        "detail.core",
                        Integer.toString(core.index()),
                        String.format("%.1f", core.usagePercent())
                ));
            }
        }
    }

    private void updateRam(SystemMetricsSnapshot snapshot, MetricHistory history) {
        usageLabel.setText(messages.format("detail.usage", String.format("%.1f", snapshot.ram().usagePercent())));
        setSecondaryVisible(false);
        setChartVisible(true);
        chartView.update(history);
        setListVisible(false);
        detailList.getItems().clear();
    }

    private void updateDisk(SystemMetricsSnapshot snapshot, MetricHistory history) {
        usageLabel.setText(messages.format("detail.usage", String.format("%.1f", snapshot.disk().usagePercent())));
        setSecondaryVisible(false);
        setChartVisible(true);
        chartView.update(history);
        setListVisible(true);
        detailList.getItems().clear();
        if (snapshot.diskVolumes().isEmpty()) {
            detailList.getItems().add(messages.get("unavailable"));
        } else {
            for (ResourceSnapshot volume : snapshot.diskVolumes()) {
                detailList.getItems().add(String.format("%s — %.1f %%", volume.detailLabel(), volume.usagePercent()));
            }
        }
    }

    private void updateNetwork(SystemMetricsSnapshot snapshot, MetricHistory history) {
        usageLabel.setText(messages.format(
                "detail.networkRates",
                ResourceSidebar.formatRate(snapshot.networkBytesPerSecIn()),
                ResourceSidebar.formatRate(snapshot.networkBytesPerSecOut())
        ));
        setSecondaryVisible(false);
        setChartVisible(true);
        chartView.update(history);
        setListVisible(true);
        detailList.getItems().clear();
        if (snapshot.networkInterfaces().isEmpty()) {
            detailList.getItems().add(messages.get("unavailable"));
        } else {
            for (NetworkInterfaceSnapshot nif : snapshot.networkInterfaces()) {
                String ip = nif.ipv4().isBlank() ? "" : " (" + nif.ipv4() + ")";
                detailList.getItems().add(String.format(
                        "%s%s — ↓ %s ↑ %s",
                        nif.name(),
                        ip,
                        ResourceSidebar.formatRate(nif.bytesPerSecIn()),
                        ResourceSidebar.formatRate(nif.bytesPerSecOut())
                ));
            }
        }
    }

    private void updateGpu(SystemMetricsSnapshot snapshot, MetricHistory history) {
        GpuSnapshot gpu = snapshot.gpu();
        setListVisible(true);
        detailList.getItems().clear();
        if (!gpu.available()) {
            usageLabel.setText(messages.get("detail.gpu.unavailable"));
            setSecondaryVisible(false);
            setChartVisible(false);
            detailList.getItems().add(messages.get("unavailable"));
            return;
        }
        usageLabel.setText(messages.format("detail.gpu.name", gpu.name().isBlank() ? "—" : gpu.name()));
        if (gpu.usagePercent().isPresent()) {
            secondaryLabel.setText(messages.format(
                    "detail.gpu.usage",
                    String.format("%.1f", gpu.usagePercent().get())
            ));
            setSecondaryVisible(true);
            setChartVisible(true);
            chartView.update(history);
        } else {
            setSecondaryVisible(false);
            setChartVisible(false);
        }
        if (gpu.memoryTotalBytes().isPresent()) {
            long total = gpu.memoryTotalBytes().get();
            long used = gpu.memoryUsedBytes().orElse(0L);
            detailList.getItems().add(messages.format(
                    "detail.gpu.memory",
                    ProcessTableView.formatBytes(used),
                    ProcessTableView.formatBytes(total)
            ));
        } else if (gpu.usagePercent().isEmpty()) {
            detailList.getItems().add(messages.get("unavailable"));
        }
    }

    private void setSecondaryVisible(boolean visible) {
        secondaryLabel.setVisible(visible);
        secondaryLabel.setManaged(visible);
    }

    private void setChartVisible(boolean visible) {
        chartView.setVisible(visible);
        chartView.setManaged(visible);
    }

    private void setListVisible(boolean visible) {
        listTitle.setVisible(visible);
        listTitle.setManaged(visible);
        detailList.setVisible(visible);
        detailList.setManaged(visible);
    }

    public void relocalize() {
        chartView.setTitle(messages.get("chart.history"));
        updateListTitle();
        if (usageLabel.getText() == null || usageLabel.getText().isBlank()) {
            usageLabel.setText(messages.format("detail.usage", "0.0"));
        }
    }

    private void updateListTitle() {
        listTitle.setText(switch (currentKind) {
            case DISK -> messages.get("detail.volumes");
            case CPU -> messages.get("detail.cores");
            case NETWORK -> messages.get("detail.interfaces");
            case GPU -> messages.get("sidebar.gpu");
            default -> "";
        });
    }

    public ResourceKind currentKind() {
        return currentKind;
    }
}
