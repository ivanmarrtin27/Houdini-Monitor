package com.houdini.monitor.metrics;

import com.houdini.monitor.collector.MetricCollector;
import com.houdini.monitor.domain.SystemMetricsSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Background sampler — never runs on the JavaFX application thread.
 */
public final class MetricsSampler {

    private static final Logger log = LoggerFactory.getLogger(MetricsSampler.class);

    public static final long DEFAULT_INTERVAL_MS = 1000L;

    private final MetricCollector<SystemMetricsSnapshot> collector;
    private final Consumer<SystemMetricsSnapshot> listener;
    private final long intervalMs;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> future;

    public MetricsSampler(
            MetricCollector<SystemMetricsSnapshot> collector,
            Consumer<SystemMetricsSnapshot> listener
    ) {
        this(collector, listener, DEFAULT_INTERVAL_MS);
    }

    public MetricsSampler(
            MetricCollector<SystemMetricsSnapshot> collector,
            Consumer<SystemMetricsSnapshot> listener,
            long intervalMs
    ) {
        this.collector = Objects.requireNonNull(collector, "collector");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.intervalMs = intervalMs <= 0 ? DEFAULT_INTERVAL_MS : intervalMs;
    }

    public synchronized void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "houdini-metrics-sampler");
            thread.setDaemon(true);
            return thread;
        });
        future = executor.scheduleAtFixedRate(this::safeSample, 0, intervalMs, TimeUnit.MILLISECONDS);
        log.debug("Metrics sampler started (interval={}ms)", intervalMs);
    }

    private void safeSample() {
        try {
            SystemMetricsSnapshot snapshot = collector.collect();
            listener.accept(snapshot);
        } catch (Throwable ex) {
            log.warn("Metrics sampling failed: {}", ex.toString());
        }
    }

    /**
     * Two-phase shutdown: cancel scheduled work, then terminate the executor.
     */
    public synchronized void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        if (future != null) {
            future.cancel(false);
            future = null;
        }
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    executor.awaitTermination(1, TimeUnit.SECONDS);
                }
            } catch (InterruptedException ex) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            } finally {
                executor = null;
            }
        }
        log.debug("Metrics sampler stopped");
    }

    public boolean isRunning() {
        return running.get();
    }

    public long intervalMs() {
        return intervalMs;
    }
}
