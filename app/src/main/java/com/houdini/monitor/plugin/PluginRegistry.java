package com.houdini.monitor.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * In-memory plugin registry. Phase 1 keeps an empty registry as a structural placeholder.
 */
public final class PluginRegistry {

    private static final Logger log = LoggerFactory.getLogger(PluginRegistry.class);

    private final List<MonitorPlugin> plugins = new ArrayList<>();

    public synchronized void register(MonitorPlugin plugin) {
        plugins.add(Objects.requireNonNull(plugin, "plugin"));
        log.info("Registered plugin {} ({})", plugin.id(), plugin.displayName());
    }

    public synchronized List<MonitorPlugin> plugins() {
        return Collections.unmodifiableList(new ArrayList<>(plugins));
    }

    public synchronized void startAll() {
        for (MonitorPlugin plugin : plugins) {
            try {
                plugin.onStart();
            } catch (RuntimeException ex) {
                log.warn("Plugin {} failed to start: {}", plugin.id(), ex.getMessage());
            }
        }
    }

    public synchronized void stopAll() {
        for (MonitorPlugin plugin : plugins) {
            try {
                plugin.onStop();
            } catch (RuntimeException ex) {
                log.warn("Plugin {} failed to stop: {}", plugin.id(), ex.getMessage());
            }
        }
    }
}
