package com.houdini.monitor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Locale;
import java.util.Objects;

/**
 * Immutable persistent application settings (JSON-backed).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AppSettings(
        ThemeMode themeMode,
        String localeTag,
        boolean alwaysOnTop,
        long sampleIntervalMs,
        int historyCapacity,
        ThresholdSettings thresholds,
        WindowSettings window,
        ColumnSettings columns
) {
    public static final long DEFAULT_SAMPLE_INTERVAL_MS = 1000L;
    public static final int DEFAULT_HISTORY_CAPACITY = 60;

    public enum ThemeMode {
        LIGHT,
        DARK,
        SYSTEM
    }

    public static AppSettings defaults() {
        return new AppSettings(
                ThemeMode.SYSTEM,
                "es",
                false,
                DEFAULT_SAMPLE_INTERVAL_MS,
                DEFAULT_HISTORY_CAPACITY,
                ThresholdSettings.defaults(),
                WindowSettings.defaults(),
                ColumnSettings.defaults()
        );
    }

    public AppSettings {
        themeMode = themeMode == null ? ThemeMode.SYSTEM : themeMode;
        localeTag = normalizeLocale(localeTag);
        sampleIntervalMs = sampleIntervalMs <= 0 ? DEFAULT_SAMPLE_INTERVAL_MS : sampleIntervalMs;
        historyCapacity = historyCapacity <= 0 ? DEFAULT_HISTORY_CAPACITY : historyCapacity;
        thresholds = thresholds == null ? ThresholdSettings.defaults() : thresholds;
        window = window == null ? WindowSettings.defaults() : window;
        columns = columns == null ? ColumnSettings.defaults() : columns;
    }

    public Locale locale() {
        return "en".equalsIgnoreCase(localeTag) ? Locale.ENGLISH : Locale.forLanguageTag("es");
    }

    public AppSettings withThemeMode(ThemeMode mode) {
        return new AppSettings(
                Objects.requireNonNull(mode, "mode"),
                localeTag,
                alwaysOnTop,
                sampleIntervalMs,
                historyCapacity,
                thresholds,
                window,
                columns
        );
    }

    public AppSettings withLocaleTag(String tag) {
        return new AppSettings(
                themeMode,
                normalizeLocale(tag),
                alwaysOnTop,
                sampleIntervalMs,
                historyCapacity,
                thresholds,
                window,
                columns
        );
    }

    public AppSettings withAlwaysOnTop(boolean value) {
        return new AppSettings(
                themeMode,
                localeTag,
                value,
                sampleIntervalMs,
                historyCapacity,
                thresholds,
                window,
                columns
        );
    }

    public AppSettings withSampleIntervalMs(long intervalMs) {
        return new AppSettings(
                themeMode,
                localeTag,
                alwaysOnTop,
                intervalMs,
                historyCapacity,
                thresholds,
                window,
                columns
        );
    }

    public AppSettings withWindow(WindowSettings windowSettings) {
        return new AppSettings(
                themeMode,
                localeTag,
                alwaysOnTop,
                sampleIntervalMs,
                historyCapacity,
                thresholds,
                Objects.requireNonNull(windowSettings, "windowSettings"),
                columns
        );
    }

    public AppSettings withThresholds(ThresholdSettings thresholdSettings) {
        return new AppSettings(
                themeMode,
                localeTag,
                alwaysOnTop,
                sampleIntervalMs,
                historyCapacity,
                Objects.requireNonNull(thresholdSettings, "thresholdSettings"),
                window,
                columns
        );
    }

    public AppSettings withColumns(ColumnSettings columnSettings) {
        return new AppSettings(
                themeMode,
                localeTag,
                alwaysOnTop,
                sampleIntervalMs,
                historyCapacity,
                thresholds,
                window,
                Objects.requireNonNull(columnSettings, "columnSettings")
        );
    }

    private static String normalizeLocale(String tag) {
        if (tag != null && tag.trim().equalsIgnoreCase("en")) {
            return "en";
        }
        return "es";
    }
}
