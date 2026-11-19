package com.houdini.monitor.storage;

import com.houdini.monitor.model.AppSettings;

import java.io.IOException;

/**
 * Loads and persists {@link AppSettings}.
 */
public interface SettingsStore {
    AppSettings load();

    void save(AppSettings settings) throws IOException;
}
