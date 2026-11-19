package com.houdini.monitor.domain;

import java.util.Objects;

public final class ResourceSnapshot {
    private final ResourceKind kind;
    private final long timestampEpochMs;
    private final double usagePercent;
    private final String detailLabel;

    public ResourceSnapshot(ResourceKind kind, long timestampEpochMs, double usagePercent, String detailLabel) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.timestampEpochMs = timestampEpochMs;
        this.usagePercent = clampPercent(usagePercent);
        this.detailLabel = detailLabel == null ? "" : detailLabel;
    }

    public static ResourceSnapshot of(ResourceKind kind, long timestampEpochMs, double usagePercent) {
        return new ResourceSnapshot(kind, timestampEpochMs, usagePercent, "");
    }

    private static double clampPercent(double value) {
        if (Double.isNaN(value) || value < 0.0) {
            return 0.0;
        }
        return Math.min(100.0, value);
    }

    public ResourceKind kind() {
        return kind;
    }

    public long timestampEpochMs() {
        return timestampEpochMs;
    }

    public double usagePercent() {
        return usagePercent;
    }

    public String detailLabel() {
        return detailLabel;
    }
}
