package com.houdini.monitor.service;

import com.houdini.monitor.model.AppSettings;
import com.houdini.monitor.storage.JsonSettingsStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreferencesServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void parseThemeFallsBackToSystem() {
        assertEquals(PreferencesService.ThemeMode.DARK, PreferencesService.parseTheme("dark"));
        assertEquals(PreferencesService.ThemeMode.SYSTEM, PreferencesService.parseTheme("nope"));
        assertEquals(PreferencesService.ThemeMode.SYSTEM, PreferencesService.parseTheme(null));
    }

    @Test
    void parseLocaleDefaultsToSpanish() {
        assertEquals(Locale.ENGLISH, PreferencesService.parseLocale("en"));
        assertEquals("es", PreferencesService.parseLocale("fr").getLanguage());
        assertEquals("es", PreferencesService.parseLocale(null).getLanguage());
    }

    @Test
    void persistsAndReloadsValuesViaJson() {
        Path path = tempDir.resolve("prefs.json");
        JsonSettingsStore store = new JsonSettingsStore(path);

        PreferencesService first = new PreferencesService(store);
        first.setThemeMode(PreferencesService.ThemeMode.DARK);
        first.setLocale(Locale.ENGLISH);
        first.setAlwaysOnTop(true);
        first.setSampleIntervalMs(1500L);

        PreferencesService second = new PreferencesService(new JsonSettingsStore(path));
        assertEquals(PreferencesService.ThemeMode.DARK, second.themeMode());
        assertEquals(Locale.ENGLISH, second.locale());
        assertTrue(second.alwaysOnTop());
        assertEquals(1500L, second.sampleIntervalMs());
        assertEquals(AppSettings.ThemeMode.DARK, second.snapshot().themeMode());
    }

    @Test
    void defaultsWhenEmpty() {
        PreferencesService service = new PreferencesService(new JsonSettingsStore(tempDir.resolve("empty.json")));
        assertEquals(PreferencesService.ThemeMode.SYSTEM, service.themeMode());
        assertEquals("es", service.locale().getLanguage());
        assertFalse(service.alwaysOnTop());
        assertEquals(AppSettings.DEFAULT_SAMPLE_INTERVAL_MS, service.sampleIntervalMs());
    }
}
