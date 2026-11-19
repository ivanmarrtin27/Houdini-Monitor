package com.houdini.monitor.service;

import com.houdini.monitor.domain.ResourceKind;
import com.houdini.monitor.domain.ResourceSnapshot;
import com.houdini.monitor.domain.SystemMetricsSnapshot;
import com.houdini.monitor.model.CircularBuffer;
import com.houdini.monitor.model.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Central in-memory hub for immutable metric snapshots and short histories.
 * Collectors push snapshots; UI/services subscribe. Never touches the FX thread itself.
 */
public final class MetricsHub {

    private static final Logger log = LoggerFactory.getLogger(MetricsHub.class);

    private final Clock clock;
    private final int historyCapacity;
    private final Map<ResourceKind, CircularBuffer<ResourceSnapshot>> histories =
            new EnumMap<>(ResourceKind.class);
    private final List<Consumer<SystemMetricsSnapshot>> listeners = new CopyOnWriteArrayList<>();
    private volatile SystemMetricsSnapshot latest;
    private volatile long lastPublishEpochMs;

    public MetricsHub(int historyCapacity) {
        this(historyCapacity, Clock.system());
    }

    public MetricsHub(int historyCapacity, Clock clock) {
        if (historyCapacity <= 0) {
            throw new IllegalArgumentException("historyCapacity must be > 0");
        }
        this.historyCapacity = historyCapacity;
        this.clock = Objects.requireNonNull(clock, "clock");
        for (ResourceKind kind : List.of(
                ResourceKind.CPU,
                ResourceKind.RAM,
                ResourceKind.DISK,
                ResourceKind.NETWORK,
                ResourceKind.GPU
        )) {
            histories.put(kind, new CircularBuffer<>(historyCapacity));
        }
    }

    public int historyCapacity() {
        return historyCapacity;
    }

    public void addListener(Consumer<SystemMetricsSnapshot> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
        SystemMetricsSnapshot current = latest;
        if (current != null) {
            listener.accept(current);
        }
    }

    public void removeListener(Consumer<SystemMetricsSnapshot> listener) {
        listeners.remove(listener);
    }

    /**
     * Publishes an immutable snapshot into histories and notifies listeners.
     */
    public void publish(SystemMetricsSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        histories.get(ResourceKind.CPU).add(snapshot.cpu());
        histories.get(ResourceKind.RAM).add(snapshot.ram());
        histories.get(ResourceKind.DISK).add(snapshot.disk());
        histories.get(ResourceKind.NETWORK).add(snapshot.network());
        if (snapshot.gpu().available() && snapshot.gpu().usagePercent().isPresent()) {
            histories.get(ResourceKind.GPU).add(new ResourceSnapshot(
                    ResourceKind.GPU,
                    snapshot.capturedAtEpochMs(),
                    snapshot.gpu().usagePercent().orElse(0.0),
                    snapshot.gpu().name()
            ));
        }
        latest = snapshot;
        lastPublishEpochMs = clock.millis();
        for (Consumer<SystemMetricsSnapshot> listener : listeners) {
            try {
                listener.accept(snapshot);
            } catch (RuntimeException ex) {
                log.warn("Metrics listener failed: {}", ex.getMessage());
            }
        }
    }

    public SystemMetricsSnapshot latest() {
        return latest;
    }

    public long lastPublishEpochMs() {
        return lastPublishEpochMs;
    }

    public List<ResourceSnapshot> history(ResourceKind kind) {
        CircularBuffer<ResourceSnapshot> buffer = histories.get(kind);
        if (buffer == null) {
            return List.of();
        }
        return buffer.snapshot();
    }

    public CircularBuffer<ResourceSnapshot> buffer(ResourceKind kind) {
        return histories.get(kind);
    }
}
