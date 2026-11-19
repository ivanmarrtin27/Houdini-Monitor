package com.houdini.monitor.service;

import com.houdini.monitor.collector.MetricCollector;
import com.houdini.monitor.domain.MetricHistory;
import com.houdini.monitor.domain.ResourceKind;
import com.houdini.monitor.domain.ResourceSnapshot;
import com.houdini.monitor.domain.SystemMetricsSnapshot;
import com.houdini.monitor.metrics.MetricsSampler;
import com.houdini.monitor.metrics.OshiMetricsCollector;
import com.houdini.monitor.model.AppSettings;
import com.houdini.monitor.model.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Orchestrates background sampling and exposes histories via {@link MetricsHub}.
 */
public final class MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);

    private final MetricsHub hub;
    private final MetricHistory cpuHistory;
    private final MetricHistory ramHistory;
    private final MetricHistory diskHistory;
    private final MetricHistory networkHistory;
    private final MetricHistory gpuHistory;
    private final MetricsSampler sampler;
    private final Clock clock;
    private final long appStartEpochMs;

    public MonitoringService() {
        this(AppSettings.DEFAULT_HISTORY_CAPACITY, AppSettings.DEFAULT_SAMPLE_INTERVAL_MS);
    }

    public MonitoringService(int historyCapacity, long sampleIntervalMs) {
        this(new OshiMetricsCollector(), historyCapacity, sampleIntervalMs, Clock.system());
    }

    public MonitoringService(
            MetricCollector<SystemMetricsSnapshot> collector,
            int historyCapacity,
            long sampleIntervalMs,
            Clock clock
    ) {
        Objects.requireNonNull(collector, "collector");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.appStartEpochMs = clock.millis();
        this.hub = new MetricsHub(historyCapacity, clock);
        this.cpuHistory = new MetricHistory(ResourceKind.CPU, historyCapacity);
        this.ramHistory = new MetricHistory(ResourceKind.RAM, historyCapacity);
        this.diskHistory = new MetricHistory(ResourceKind.DISK, historyCapacity);
        this.networkHistory = new MetricHistory(ResourceKind.NETWORK, historyCapacity);
        this.gpuHistory = new MetricHistory(ResourceKind.GPU, historyCapacity);
        this.sampler = new MetricsSampler(collector, this::onSnapshot, sampleIntervalMs);
        hub.addListener(this::mirrorIntoLegacyHistories);
    }

    public MetricsHub hub() {
        return hub;
    }

    public void addListener(Consumer<SystemMetricsSnapshot> listener) {
        hub.addListener(listener);
    }

    public void start() {
        log.info("Starting monitoring sampler");
        sampler.start();
    }

    public void stop() {
        log.info("Stopping monitoring sampler");
        sampler.stop();
    }

    public MetricHistory cpuHistory() {
        return cpuHistory;
    }

    public MetricHistory ramHistory() {
        return ramHistory;
    }

    public MetricHistory diskHistory() {
        return diskHistory;
    }

    public MetricHistory networkHistory() {
        return networkHistory;
    }

    public MetricHistory gpuHistory() {
        return gpuHistory;
    }

    public SystemMetricsSnapshot latest() {
        return hub.latest();
    }

    public long appUptimeSeconds() {
        return Math.max(0L, (clock.millis() - appStartEpochMs) / 1000L);
    }

    public boolean isRunning() {
        return sampler.isRunning();
    }

    private void onSnapshot(SystemMetricsSnapshot snapshot) {
        hub.publish(snapshot);
    }

    private void mirrorIntoLegacyHistories(SystemMetricsSnapshot snapshot) {
        cpuHistory.add(snapshot.cpu());
        ramHistory.add(snapshot.ram());
        diskHistory.add(snapshot.disk());
        networkHistory.add(snapshot.network());
        if (snapshot.gpu().available() && snapshot.gpu().usagePercent().isPresent()) {
            gpuHistory.add(new ResourceSnapshot(
                    ResourceKind.GPU,
                    snapshot.capturedAtEpochMs(),
                    snapshot.gpu().usagePercent().orElse(0.0),
                    snapshot.gpu().name()
            ));
        }
    }

    public List<ResourceSnapshot> hubHistory(ResourceKind kind) {
        return hub.history(kind);
    }
}
