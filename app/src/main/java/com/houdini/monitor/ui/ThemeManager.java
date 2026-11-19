package com.houdini.monitor.ui;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.houdini.monitor.service.PreferencesService;
import javafx.application.Application;
import javafx.scene.Scene;

import java.util.Objects;

public final class ThemeManager {

    private static final String ACCENT_STYLESHEET = Objects.requireNonNull(
            ThemeManager.class.getResource("/styles/houdini-accent.css")).toExternalForm();

    public void apply(Scene scene, PreferencesService.ThemeMode mode) {
        boolean dark = switch (mode) {
            case DARK -> true;
            case LIGHT -> false;
            case SYSTEM -> isSystemDark();
        };
        Application.setUserAgentStylesheet(dark ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
        if (scene != null) {
            scene.getRoot().getStyleClass().removeAll("dark", "light");
            scene.getRoot().getStyleClass().add(dark ? "dark" : "light");
            if (!scene.getStylesheets().contains(ACCENT_STYLESHEET)) {
                scene.getStylesheets().add(ACCENT_STYLESHEET);
            }
        }
    }

    public boolean isSystemDark() {
        String osTheme = System.getProperty("houdini.forceDark");
        if (osTheme != null) {
            return Boolean.parseBoolean(osTheme);
        }
        try {
            Process process = new ProcessBuilder(
                    "reg", "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v", "AppsUseLightTheme"
            ).redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes());
            process.waitFor();
            if (output.contains("0x0")) {
                return true;
            }
            if (output.contains("0x1")) {
                return false;
            }
        } catch (Exception ignored) {
            // default light
        }
        return false;
    }
}
