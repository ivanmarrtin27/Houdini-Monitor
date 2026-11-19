package com.houdini.monitor.metrics;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiskUsageAggregatorTest {

    @Test
    void aggregatesUsedPercentAcrossVolumes() {
        double percent = DiskUsageAggregator.aggregateUsedPercent(List.of(
                new DiskUsageAggregator.VolumeUsage("C:\\", 50L, 100L),
                new DiskUsageAggregator.VolumeUsage("D:\\", 25L, 100L)
        ));
        // used 75 of 200 = 37.5%
        assertEquals(37.5, percent, 0.001);
    }

    @Test
    void returnsZeroWhenNoVolumes() {
        assertEquals(0.0, DiskUsageAggregator.aggregateUsedPercent(List.of()));
    }

    @Test
    void ignoresZeroTotalVolumes() {
        double percent = DiskUsageAggregator.aggregateUsedPercent(List.of(
                new DiskUsageAggregator.VolumeUsage("X:\\", 10L, 0L),
                new DiskUsageAggregator.VolumeUsage("C:\\", 40L, 100L)
        ));
        assertEquals(40.0, percent, 0.001);
    }
}
