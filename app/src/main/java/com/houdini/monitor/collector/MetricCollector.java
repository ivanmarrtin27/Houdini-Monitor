package com.houdini.monitor.collector;

/**
 * Collects an immutable metrics snapshot of type {@code T}.
 * Implementations must not block the JavaFX application thread.
 *
 * @param <T> immutable snapshot type
 */
@FunctionalInterface
public interface MetricCollector<T> {
    T collect();
}
