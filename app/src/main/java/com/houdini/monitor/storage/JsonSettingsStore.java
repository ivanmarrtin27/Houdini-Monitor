package com.houdini.monitor.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.houdini.monitor.model.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * JSON settings under the user config directory ({@code ~/.houdini-monitor/settings.json}).
 */
public final class JsonSettingsStore implements SettingsStore {

    private static final Logger log = LoggerFactory.getLogger(JsonSettingsStore.class);
    private static final String CONFIG_DIR_NAME = ".houdini-monitor";
    private static final String SETTINGS_FILE = "settings.json";

    private final Path settingsPath;
    private final ObjectMapper mapper;

    public JsonSettingsStore() {
        this(defaultSettingsPath());
    }

    public JsonSettingsStore(Path settingsPath) {
        this.settingsPath = Objects.requireNonNull(settingsPath, "settingsPath");
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static Path defaultConfigDir() {
        return Path.of(System.getProperty("user.home"), CONFIG_DIR_NAME);
    }

    public static Path defaultSettingsPath() {
        return defaultConfigDir().resolve(SETTINGS_FILE);
    }

    public Path settingsPath() {
        return settingsPath;
    }

    @Override
    public AppSettings load() {
        if (!Files.isRegularFile(settingsPath)) {
            AppSettings defaults = AppSettings.defaults();
            log.info("No settings file at {}; writing defaults", settingsPath);
            saveQuietly(defaults);
            return defaults;
        }
        try {
            AppSettings loaded = mapper.readValue(settingsPath.toFile(), AppSettings.class);
            log.debug("Loaded settings from {}", settingsPath);
            return loaded == null ? AppSettings.defaults() : loaded;
        } catch (IOException ex) {
            log.warn("Failed to read settings from {}; using defaults: {}", settingsPath, ex.getMessage());
            return AppSettings.defaults();
        }
    }

    @Override
    public void save(AppSettings settings) throws IOException {
        Objects.requireNonNull(settings, "settings");
        Path parent = settingsPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = settingsPath.resolveSibling(settingsPath.getFileName() + ".tmp");
        mapper.writeValue(temp.toFile(), settings);
        try {
            Files.move(temp, settingsPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicFailed) {
            Files.move(temp, settingsPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        log.debug("Saved settings to {}", settingsPath);
    }

    public void saveQuietly(AppSettings settings) {
        try {
            save(settings);
        } catch (IOException ex) {
            log.warn("Failed to persist settings to {}: {}", settingsPath, ex.getMessage());
        }
    }
}
