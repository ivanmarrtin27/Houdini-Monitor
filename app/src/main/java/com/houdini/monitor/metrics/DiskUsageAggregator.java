package com.houdini.monitor.metrics;

import java.util.List;

public final class DiskUsageAggregator {

    private DiskUsageAggregator() {
    }

    public record VolumeUsage(String mount, long usedBytes, long totalBytes) {
    }

    public static double aggregateUsedPercent(List<VolumeUsage> volumes) {
        if (volumes == null || volumes.isEmpty()) {
            return 0.0;
        }
        long used = 0;
        long total = 0;
        for (VolumeUsage volume : volumes) {
            if (volume.totalBytes() <= 0) {
                continue;
            }
            used += Math.max(0L, volume.usedBytes());
            total += volume.totalBytes();
        }
        if (total <= 0) {
            return 0.0;
        }
        return (used * 100.0) / total;
    }
}
