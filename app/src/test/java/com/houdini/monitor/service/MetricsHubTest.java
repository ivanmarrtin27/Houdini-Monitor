package com.houdini.monitor.service;

import com.houdini.monitor.domain.GpuSnapshot;
import com.houdini.monitor.domain.ResourceKind;
import com.houdini.monitor.domain.ResourceSnapshot;
import com.houdini.monitor.domain.SystemMetricsSnapshot;
import com.houdini.monitor.model.Clock;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MetricsHubTest {

    @Test
    void publishesImmutableSnapshotAndHistory() {
        AtomicLong now = new AtomicLong(5_000L);
        Clock clock = now::get;
        MetricsHub hub = new MetricsHub(2, clock);

        AtomicReference<SystemMetricsSnapshot> received = new AtomicReference<>();
        hub.addListener(received::set);

        SystemMetricsSnapshot snap = sample(1_000L, 10.0);
        hub.publish(snap);

        assertNotNull(hub.latest());
        assertEquals(snap, received.get());
        assertEquals(5_000L, hub.lastPublishEpochMs());
        assertEquals(1, hub.history(ResourceKind.CPU).size());
        assertEquals(10.0, hub.history(ResourceKind.CPU).getFirst().usagePercent());

        hub.publish(sample(2_000L, 20.0));
        hub.publish(sample(3_000L, 30.0));
        assertEquals(2, hub.history(ResourceKind.CPU).size());
        assertEquals(20.0, hub.history(ResourceKind.CPU).getFirst().usagePercent());
        assertEquals(30.0, hub.history(ResourceKind.CPU).getLast().usagePercent());
    }

    private static SystemMetricsSnapshot sample(long epochMs, double cpu) {
        return new SystemMetricsSnapshot(
                epochMs,
                ResourceSnapshot.of(ResourceKind.CPU, epochMs, cpu),
                List.of(),
                ResourceSnapshot.of(ResourceKind.RAM, epochMs, 1.0),
                ResourceSnapshot.of(ResourceKind.DISK, epochMs, 1.0),
                List.of(),
                ResourceSnapshot.of(ResourceKind.NETWORK, epochMs, 1.0),
                0.0,
                0.0,
                List.of(),
                GpuSnapshot.unavailable(),
                List.of(),
                epochMs - 60_000L,
                60L
        );
    }
}
