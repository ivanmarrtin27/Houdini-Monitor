package com.houdini.monitor.domain;

import com.houdini.monitor.model.CircularBuffer;

import java.util.List;
import java.util.Objects;

/**
 * Typed history of {@link ResourceSnapshot} samples backed by {@link CircularBuffer}.
 */
public final class MetricHistory {
    public static final int DEFAULT_CAPACITY = 60;

    private final ResourceKind kind;
    private final int capacity;
    private final CircularBuffer<ResourceSnapshot> buffer;

    public MetricHistory(ResourceKind kind, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        this.kind = Objects.requireNonNull(kind, "kind");
        this.capacity = capacity;
        this.buffer = new CircularBuffer<>(capacity);
    }

    public ResourceKind kind() {
        return kind;
    }

    public int capacity() {
        return capacity;
    }

    public void add(ResourceSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        if (snapshot.kind() != kind) {
            throw new IllegalArgumentException("snapshot kind " + snapshot.kind() + " != " + kind);
        }
        buffer.add(snapshot);
    }

    public List<ResourceSnapshot> samples() {
        return buffer.snapshot();
    }
}
