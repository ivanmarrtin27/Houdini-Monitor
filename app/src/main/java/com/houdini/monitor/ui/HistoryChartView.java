package com.houdini.monitor.ui;

import com.houdini.monitor.domain.MetricHistory;
import com.houdini.monitor.domain.ResourceKind;
import com.houdini.monitor.domain.ResourceSnapshot;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public final class HistoryChartView extends VBox {

    private final LineChart<Number, Number> chart;
    private final XYChart.Series<Number, Number> series = new XYChart.Series<>();
    private final NumberAxis xAxis = new NumberAxis();
    private final NumberAxis yAxis = new NumberAxis(0, 100, 20);

    public HistoryChartView() {
        xAxis.setLabel("");
        xAxis.setTickLabelsVisible(false);
        xAxis.setMinorTickVisible(false);
        yAxis.setAutoRanging(false);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);
        chart.setMinHeight(160);
        chart.getData().add(series);
        chart.getStyleClass().add("houdini-history-chart");
        VBox.setVgrow(chart, Priority.ALWAYS);
        getChildren().add(chart);
        setSpacing(6);
    }

    public void setTitle(String title) {
        chart.setTitle(title);
    }

    public void update(MetricHistory history) {
        List<ResourceSnapshot> samples = history.samples();
        series.getData().clear();
        int index = 0;
        double max = 1.0;
        for (ResourceSnapshot sample : samples) {
            double value = sample.usagePercent();
            max = Math.max(max, value);
            series.getData().add(new XYChart.Data<>(index++, value));
        }
        xAxis.setAutoRanging(true);
        if (history.kind() == ResourceKind.NETWORK) {
            yAxis.setAutoRanging(true);
            yAxis.setForceZeroInRange(true);
        } else {
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(100);
            yAxis.setTickUnit(20);
        }
    }
}
