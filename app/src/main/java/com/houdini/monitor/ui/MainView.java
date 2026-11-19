package com.houdini.monitor.ui;

import com.houdini.monitor.domain.MetricHistory;
import com.houdini.monitor.domain.ProcessInfo;
import com.houdini.monitor.domain.ResourceKind;
import com.houdini.monitor.domain.SystemMetricsSnapshot;
import com.houdini.monitor.domain.kill.KillOutcome;
import com.houdini.monitor.i18n.Messages;
import com.houdini.monitor.metrics.ProcessTerminator;
import com.houdini.monitor.model.WindowSettings;
import com.houdini.monitor.service.MonitoringService;
import com.houdini.monitor.service.PreferencesService;
import com.houdini.monitor.util.FormatUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Locale;

public final class MainView extends BorderPane {

    private final Stage stage;
    private final MonitoringService monitoringService;
    private final PreferencesService preferencesService;
    private final Messages messages;
    private final ThemeManager themeManager = new ThemeManager();
    private final ProcessTerminator processTerminator = new ProcessTerminator();

    private final ResourceSidebar sidebar;
    private final ResourceDetailPane detailPane;
    private final ProcessTableView processTable;
    private final StackPane detailStack = new StackPane();
    private final KillConfirmDialog killDialog;
    private final StatusBar statusBar;

    private final ComboBox<String> themeCombo = new ComboBox<>();
    private final ComboBox<String> languageCombo = new ComboBox<>();
    private final Label themeLabel = new Label();
    private final Label languageLabel = new Label();
    private final CheckBox alwaysOnTopCheck = new CheckBox();
    private final Label systemUptimeLabel = new Label();
    private final Label appUptimeLabel = new Label();
    private boolean processesVisible;
    private TrayService trayService;
    private long lastUpdateEpochMs;

    public MainView(
            Stage stage,
            MonitoringService monitoringService,
            PreferencesService preferencesService,
            Messages messages
    ) {
        this.stage = stage;
        this.monitoringService = monitoringService;
        this.preferencesService = preferencesService;
        this.messages = messages;
        this.killDialog = new KillConfirmDialog(messages);
        this.sidebar = new ResourceSidebar(messages, this::onResourceSelected);
        this.detailPane = new ResourceDetailPane(messages);
        this.processTable = new ProcessTableView(messages, this::onEndProcess);
        this.statusBar = new StatusBar(messages);

        detailStack.getChildren().addAll(detailPane, processTable);
        processTable.setVisible(false);
        processTable.setManaged(false);

        setLeft(sidebar);
        setCenter(detailStack);
        setTop(buildToolbar());
        setBottom(statusBar);
        setPadding(new Insets(0));

        sidebar.selectDefault();
        monitoringService.addListener(snapshot -> Platform.runLater(() -> applySnapshot(snapshot)));
        restorePreferenceControls();
        restoreWindowGeometry();
        relocalizeChrome();
        themeManager.apply(null, preferencesService.themeMode());
        stage.setAlwaysOnTop(preferencesService.alwaysOnTop());
        statusBar.setIntervalMs(preferencesService.sampleIntervalMs());
        statusBar.setStatusKey("status.ready");
    }

    private HBox buildToolbar() {
        themeCombo.getItems().setAll("light", "dark", "system");
        themeCombo.setOnAction(e -> {
            PreferencesService.ThemeMode mode = switch (themeCombo.getValue()) {
                case "light" -> PreferencesService.ThemeMode.LIGHT;
                case "dark" -> PreferencesService.ThemeMode.DARK;
                default -> PreferencesService.ThemeMode.SYSTEM;
            };
            preferencesService.setThemeMode(mode);
            if (getScene() != null) {
                themeManager.apply(getScene(), mode);
            }
        });

        languageCombo.getItems().setAll("es", "en");
        languageCombo.setOnAction(e -> {
            Locale locale = "en".equals(languageCombo.getValue()) ? Locale.ENGLISH : Locale.forLanguageTag("es");
            preferencesService.setLocale(locale);
            messages.setLocale(locale);
            relocalizeChrome();
            if (trayService != null) {
                trayService.relocalize();
            }
        });

        alwaysOnTopCheck.setOnAction(e -> {
            boolean value = alwaysOnTopCheck.isSelected();
            preferencesService.setAlwaysOnTop(value);
            stage.setAlwaysOnTop(value);
        });

        systemUptimeLabel.getStyleClass().add("uptime-label");
        appUptimeLabel.getStyleClass().add("uptime-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(
                10,
                themeLabel,
                themeCombo,
                languageLabel,
                languageCombo,
                alwaysOnTopCheck,
                spacer,
                systemUptimeLabel,
                appUptimeLabel
        );
        bar.getStyleClass().add("toolbar-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 12, 8, 12));
        return bar;
    }

    private void restorePreferenceControls() {
        String themeKey = switch (preferencesService.themeMode()) {
            case LIGHT -> "light";
            case DARK -> "dark";
            case SYSTEM -> "system";
        };
        themeCombo.getSelectionModel().select(themeKey);
        languageCombo.getSelectionModel().select(
                "en".equalsIgnoreCase(preferencesService.locale().getLanguage()) ? "en" : "es"
        );
        alwaysOnTopCheck.setSelected(preferencesService.alwaysOnTop());
    }

    private void restoreWindowGeometry() {
        WindowSettings window = preferencesService.window();
        stage.setWidth(window.width());
        stage.setHeight(window.height());
        if (window.hasPosition()) {
            stage.setX(window.x().doubleValue());
            stage.setY(window.y().doubleValue());
        }
        if (window.maximized()) {
            stage.setMaximized(true);
        }
    }

    public void persistWindowNow() {
        WindowSettings window = new WindowSettings(
                stage.getWidth(),
                stage.getHeight(),
                stage.getX(),
                stage.getY(),
                stage.isMaximized()
        );
        preferencesService.setWindow(window);
    }

    public void installAccelerators() {
        if (getScene() == null) {
            return;
        }
        getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN),
                () -> {
                    if (processesVisible) {
                        processTable.focusSearch();
                    }
                }
        );
    }

    private void onResourceSelected(ResourceKind kind) {
        if (processTable == null || detailPane == null) {
            return;
        }
        processesVisible = kind == ResourceKind.PROCESSES;
        processTable.setVisible(processesVisible);
        processTable.setManaged(processesVisible);
        detailPane.setVisible(!processesVisible);
        detailPane.setManaged(!processesVisible);
        if (!processesVisible) {
            detailPane.show(kind);
            SystemMetricsSnapshot latest = monitoringService.latest();
            if (latest != null) {
                detailPane.update(latest, historyFor(kind));
            }
        } else {
            SystemMetricsSnapshot latest = monitoringService.latest();
            if (latest != null) {
                processTable.updateProcesses(latest.processes());
            }
        }
    }

    private void applySnapshot(SystemMetricsSnapshot snapshot) {
        sidebar.updateMetrics(snapshot);
        updateUptimeLabels(snapshot);
        lastUpdateEpochMs = snapshot.capturedAtEpochMs();
        statusBar.setStatusKey("status.sampling");
        statusBar.setLastUpdateEpochMs(lastUpdateEpochMs);
        statusBar.setIntervalMs(preferencesService.sampleIntervalMs());
        if (processesVisible) {
            processTable.updateProcesses(snapshot.processes());
        } else {
            detailPane.update(snapshot, historyFor(detailPane.currentKind()));
        }
    }

    private void updateUptimeLabels(SystemMetricsSnapshot snapshot) {
        systemUptimeLabel.setText(messages.format("uptime.system", formatDuration(snapshot.systemUptimeSeconds())));
        appUptimeLabel.setText(messages.format("uptime.app", formatDuration(monitoringService.appUptimeSeconds())));
    }

    private MetricHistory historyFor(ResourceKind kind) {
        return switch (kind) {
            case RAM -> monitoringService.ramHistory();
            case DISK -> monitoringService.diskHistory();
            case NETWORK -> monitoringService.networkHistory();
            case GPU -> monitoringService.gpuHistory();
            default -> monitoringService.cpuHistory();
        };
    }

    private void onEndProcess(ProcessInfo process) {
        KillOutcome confirmation = killDialog.confirm(process);
        if (confirmation != KillOutcome.SUCCESS) {
            return;
        }
        KillOutcome outcome = processTerminator.terminate(process.pid());
        killDialog.showOutcome(outcome);
    }

    private void relocalizeChrome() {
        stage.setTitle(messages.get("app.title"));
        themeLabel.setText(messages.get("theme.label"));
        languageLabel.setText(messages.get("language.label"));
        alwaysOnTopCheck.setText(messages.get("window.alwaysOnTop"));
        themeCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(String key) {
                if (key == null) {
                    return "";
                }
                return switch (key) {
                    case "light" -> messages.get("theme.light");
                    case "dark" -> messages.get("theme.dark");
                    default -> messages.get("theme.system");
                };
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        });
        String themeValue = themeCombo.getValue();
        themeCombo.setValue(null);
        themeCombo.setValue(themeValue);

        languageCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(String key) {
                if ("en".equals(key)) {
                    return messages.get("language.en");
                }
                return messages.get("language.es");
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        });
        String langValue = languageCombo.getValue();
        languageCombo.setValue(null);
        languageCombo.setValue(langValue);

        sidebar.refreshLabels();
        detailPane.relocalize();
        processTable.relocalize();
        statusBar.relocalize(preferencesService.sampleIntervalMs(), lastUpdateEpochMs);
        if (lastUpdateEpochMs <= 0L) {
            statusBar.setStatusKey("status.ready");
        } else {
            statusBar.setStatusKey("status.sampling");
        }
        SystemMetricsSnapshot latest = monitoringService.latest();
        if (latest != null) {
            updateUptimeLabels(latest);
        }
    }

    public void applyThemeToScene() {
        if (getScene() != null) {
            themeManager.apply(getScene(), preferencesService.themeMode());
        }
    }

    public void setTrayService(TrayService trayService) {
        this.trayService = trayService;
    }

    static String formatDuration(long totalSeconds) {
        return FormatUtils.duration(totalSeconds);
    }
}
