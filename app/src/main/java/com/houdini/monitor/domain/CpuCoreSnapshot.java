package com.houdini.monitor.domain;

public final class CpuCoreSnapshot {
    private final int index;
    private final double usagePercent;

    public CpuCoreSnapshot(int index, double usagePercent) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
        this.index = index;
        this.usagePercent = clampPercent(usagePercent);
    }

    public int index() {
        return index;
    }

    public double usagePercent() {
        return usagePercent;
    }

    private static double clampPercent(double value) {
        if (Double.isNaN(value) || value < 0.0) {
            return 0.0;
        }
        return Math.min(100.0, value);
    }
}
