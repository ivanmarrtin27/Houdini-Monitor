package com.houdini.monitor.storage;

import com.houdini.monitor.model.AppSettings;
import com.houdini.monitor.model.ThresholdSettings;
import com.houdini.monitor.model.WindowSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonSettingsStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsDefaultsWhenMissing() {
        Path path = tempDir.resolve("missing.json");
        JsonSettingsStore store = new JsonSettingsStore(path);
        AppSettings settings = store.load();
        assertEquals(AppSettings.ThemeMode.SYSTEM, settings.themeMode());
        assertEquals("es", settings.localeTag());
        assertFalse(settings.alwaysOnTop());
        assertTrue(path.toFile().isFile());
    }

    @Test
    void roundTripsSettings() throws Exception {
        Path path = tempDir.resolve("settings.json");
        JsonSettingsStore store = new JsonSettingsStore(path);

        AppSettings original = AppSettings.defaults()
                .withThemeMode(AppSettings.ThemeMode.DARK)
                .withLocaleTag("en")
                .withAlwaysOnTop(true)
                .withSampleIntervalMs(2000L)
                .withWindow(new WindowSettings(1400, 900, 10.0, 20.0, false))
                .withThresholds(new ThresholdSettings(60, 85, 70, 88, 75, 92));

        store.save(original);
        assertTrue(path.toFile().isFile());

        AppSettings loaded = store.load();
        assertEquals(AppSettings.ThemeMode.DARK, loaded.themeMode());
        assertEquals("en", loaded.localeTag());
        assertTrue(loaded.alwaysOnTop());
        assertEquals(2000L, loaded.sampleIntervalMs());
        assertEquals(1400.0, loaded.window().width());
        assertEquals(10.0, loaded.window().x().doubleValue());
        assertEquals(85.0, loaded.thresholds().cpuCriticalPercent());
    }
}
