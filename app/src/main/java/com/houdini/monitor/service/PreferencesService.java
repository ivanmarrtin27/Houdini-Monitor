package com.houdini.monitor.service;

import com.houdini.monitor.model.AppSettings;
import com.houdini.monitor.model.ColumnSettings;
import com.houdini.monitor.model.ThresholdSettings;
import com.houdini.monitor.model.WindowSettings;
import com.houdini.monitor.storage.JsonSettingsStore;
import com.houdini.monitor.storage.SettingsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Objects;

/**
 * Mutable façade over immutable {@link AppSettings} persisted via {@link SettingsStore}.
 * Phase 1 replaces {@code java.util.prefs} with JSON under {@code ~/.houdini-monitor/}.
 */
public final class PreferencesService {

    private static final Logger log = LoggerFactory.getLogger(PreferencesService.class);

    /** UI-facing theme enum (mirrors {@link AppSettings.ThemeMode}). */
    public enum ThemeMode {
        LIGHT,
        DARK,
        SYSTEM;

        AppSettings.ThemeMode toModel() {
            return AppSettings.ThemeMode.valueOf(name());
        }

        static ThemeMode fromModel(AppSettings.ThemeMode mode) {
            return ThemeMode.valueOf(mode.name());
        }
    }

    private final SettingsStore store;
    private AppSettings settings;

    public PreferencesService() {
        this(new JsonSettingsStore());
    }

    public PreferencesService(SettingsStore store) {
        this.store = Objects.requireNonNull(store, "store");
        this.settings = store.load();
        log.info("Preferences loaded (theme={}, locale={}, interval={}ms)",
                settings.themeMode(), settings.localeTag(), settings.sampleIntervalMs());
    }

    public AppSettings snapshot() {
        return settings;
    }

    public ThemeMode themeMode() {
        return ThemeMode.fromModel(settings.themeMode());
    }

    public void setThemeMode(ThemeMode themeMode) {
        Objects.requireNonNull(themeMode, "themeMode");
        settings = settings.withThemeMode(themeMode.toModel());
        persist();
    }

    public Locale locale() {
        return settings.locale();
    }

    public void setLocale(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        settings = settings.withLocaleTag(locale.getLanguage());
        persist();
    }

    public boolean alwaysOnTop() {
        return settings.alwaysOnTop();
    }

    public void setAlwaysOnTop(boolean alwaysOnTop) {
        settings = settings.withAlwaysOnTop(alwaysOnTop);
        persist();
    }

    public long sampleIntervalMs() {
        return settings.sampleIntervalMs();
    }

    public void setSampleIntervalMs(long sampleIntervalMs) {
        settings = settings.withSampleIntervalMs(sampleIntervalMs);
        persist();
    }

    public int historyCapacity() {
        return settings.historyCapacity();
    }

    public ThresholdSettings thresholds() {
        return settings.thresholds();
    }

    public void setThresholds(ThresholdSettings thresholds) {
        settings = settings.withThresholds(thresholds);
        persist();
    }

    public WindowSettings window() {
        return settings.window();
    }

    public void setWindow(WindowSettings window) {
        settings = settings.withWindow(window);
        persist();
    }

    public ColumnSettings columns() {
        return settings.columns();
    }

    public void setColumns(ColumnSettings columns) {
        settings = settings.withColumns(columns);
        persist();
    }

    static ThemeMode parseTheme(String raw) {
        if (raw == null || raw.isBlank()) {
            return ThemeMode.SYSTEM;
        }
        try {
            return ThemeMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ThemeMode.SYSTEM;
        }
    }

    static Locale parseLocale(String raw) {
        if (raw != null && raw.trim().equalsIgnoreCase("en")) {
            return Locale.ENGLISH;
        }
        return Locale.forLanguageTag("es");
    }

    private void persist() {
        if (store instanceof JsonSettingsStore jsonStore) {
            jsonStore.saveQuietly(settings);
        } else {
            try {
                store.save(settings);
            } catch (Exception ex) {
                log.warn("Failed to persist preferences: {}", ex.getMessage());
            }
        }
    }
}
