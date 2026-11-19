package com.houdini.monitor.model;

/**
 * Injectable time source for deterministic tests and sampling.
 */
@FunctionalInterface
public interface Clock {
    long millis();

    static Clock system() {
        return System::currentTimeMillis;
    }
}
