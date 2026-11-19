package com.houdini.monitor.ui;

import com.houdini.monitor.i18n.Messages;
import com.houdini.monitor.util.FormatUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Objects;

/**
 * Bottom status bar: sampling interval, last update, and a short status message.
 */
public final class StatusBar extends HBox {

    private final Messages messages;
    private final Label statusLabel = new Label();
    private final Label intervalLabel = new Label();
    private final Label updatedLabel = new Label();

    public StatusBar(Messages messages) {
        this.messages = Objects.requireNonNull(messages, "messages");
        getStyleClass().add("status-bar");
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(4, 12, 4, 12));
        setSpacing(16);

        statusLabel.getStyleClass().add("status-bar-message");
        intervalLabel.getStyleClass().add("status-bar-meta");
        updatedLabel.getStyleClass().add("status-bar-meta");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(statusLabel, spacer, intervalLabel, updatedLabel);
        setStatusKey("status.ready");
        setIntervalMs(1000L);
        setLastUpdateEpochMs(0L);
    }

    public void setStatusKey(String key) {
        statusLabel.setText(messages.get(key));
    }

    public void setStatusText(String text) {
        statusLabel.setText(text == null ? "" : text);
    }

    public void setIntervalMs(long intervalMs) {
        intervalLabel.setText(messages.format("status.interval", intervalMs));
    }

    public void setLastUpdateEpochMs(long epochMs) {
        if (epochMs <= 0L) {
            updatedLabel.setText(messages.get("status.updated.never"));
            return;
        }
        long ageSec = Math.max(0L, (System.currentTimeMillis() - epochMs) / 1000L);
        updatedLabel.setText(messages.format("status.updated", FormatUtils.duration(ageSec)));
    }

    public void relocalize(long intervalMs, long lastUpdateEpochMs) {
        // Keep current semantic keys by re-applying values
        setIntervalMs(intervalMs);
        setLastUpdateEpochMs(lastUpdateEpochMs);
    }
}
