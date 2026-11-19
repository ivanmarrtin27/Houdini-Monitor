package com.houdini.monitor.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricHistoryTest {

    @Test
    void dropsOldestWhenCapacityExceeded() {
        MetricHistory history = new MetricHistory(ResourceKind.CPU, 3);
        history.add(ResourceSnapshot.of(ResourceKind.CPU, 1, 10));
        history.add(ResourceSnapshot.of(ResourceKind.CPU, 2, 20));
        history.add(ResourceSnapshot.of(ResourceKind.CPU, 3, 30));
        history.add(ResourceSnapshot.of(ResourceKind.CPU, 4, 40));

        var samples = history.samples();
        assertEquals(3, samples.size());
        assertEquals(2, samples.get(0).timestampEpochMs());
        assertEquals(4, samples.get(2).timestampEpochMs());
        assertEquals(40.0, samples.get(2).usagePercent());
    }

    @Test
    void rejectsNullSnapshot() {
        MetricHistory history = new MetricHistory(ResourceKind.RAM, 60);
        assertThrows(NullPointerException.class, () -> history.add(null));
    }

    @Test
    void rejectsWrongKind() {
        MetricHistory history = new MetricHistory(ResourceKind.DISK, 60);
        assertThrows(IllegalArgumentException.class,
                () -> history.add(ResourceSnapshot.of(ResourceKind.CPU, 1, 5)));
    }

    @Test
    void startsEmpty() {
        MetricHistory history = new MetricHistory(ResourceKind.CPU, 60);
        assertTrue(history.samples().isEmpty());
        assertEquals(60, history.capacity());
    }
}
