package com.houdini.monitor.plugin;

/**
 * Placeholder extension point for future metric/UI plugins (PulseDeck phases 8+).
 * No plugins are loaded in Phase 1.
 */
public interface MonitorPlugin {

    String id();

    String displayName();

    default void onStart() {
        // no-op
    }

    default void onStop() {
        // no-op
    }
}
