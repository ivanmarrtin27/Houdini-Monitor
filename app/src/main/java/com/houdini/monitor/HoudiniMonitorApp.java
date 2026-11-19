package com.houdini.monitor;

import com.houdini.monitor.i18n.Messages;
import com.houdini.monitor.model.WindowSettings;
import com.houdini.monitor.plugin.PluginRegistry;
import com.houdini.monitor.service.MonitoringService;
import com.houdini.monitor.service.PreferencesService;
import com.houdini.monitor.ui.MainView;
import com.houdini.monitor.ui.TrayService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoudiniMonitorApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(HoudiniMonitorApp.class);

    private MonitoringService monitoringService;
    private PreferencesService preferencesService;
    private MainView mainView;
    private TrayService trayService;
    private final PluginRegistry pluginRegistry = new PluginRegistry();
    private boolean exiting;

    @Override
    public void start(Stage stage) {
        preferencesService = new PreferencesService();
        Messages messages = new Messages(preferencesService.locale());
        monitoringService = new MonitoringService(
                preferencesService.historyCapacity(),
                preferencesService.sampleIntervalMs()
        );

        WindowSettings window = preferencesService.window();
        mainView = new MainView(stage, monitoringService, preferencesService, messages);
        Scene scene = new Scene(mainView, window.width(), window.height());
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(600);
        stage.setTitle(messages.get("app.title"));
        mainView.applyThemeToScene();
        mainView.installAccelerators();

        trayService = new TrayService(stage, messages, ignored -> exitApplication());
        boolean trayReady = trayService.install();
        mainView.setTrayService(trayService);
        if (trayReady) {
            Platform.setImplicitExit(false);
        }

        stage.setOnCloseRequest(e -> {
            if (exiting) {
                return;
            }
            mainView.persistWindowNow();
            if (trayReady) {
                e.consume();
                trayService.hideStage();
            } else {
                exitApplication();
            }
        });

        pluginRegistry.startAll();
        monitoringService.start();
        log.info("Houdini Monitor started");
        stage.show();
    }

    private void exitApplication() {
        if (exiting) {
            return;
        }
        exiting = true;
        if (mainView != null) {
            mainView.persistWindowNow();
        }
        shutdown();
        if (trayService != null) {
            trayService.uninstall();
        }
        Platform.exit();
    }

    @Override
    public void stop() {
        shutdown();
        if (trayService != null) {
            trayService.uninstall();
        }
    }

    private void shutdown() {
        pluginRegistry.stopAll();
        if (monitoringService != null) {
            monitoringService.stop();
        }
        log.info("Houdini Monitor stopped");
    }

    public static void main(String[] args) {
        // Ensure AWT toolkit initializes for SystemTray on Windows with JavaFX
        try {
            java.awt.Toolkit.getDefaultToolkit();
        } catch (Throwable ignored) {
            // Tray may be unavailable; app still runs
        }
        launch(args);
    }
}
