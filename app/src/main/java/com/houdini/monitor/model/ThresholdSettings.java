package com.houdini.monitor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Warning/critical thresholds as percentages (0–100).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ThresholdSettings(
        double cpuWarnPercent,
        double cpuCriticalPercent,
        double ramWarnPercent,
        double ramCriticalPercent,
        double diskWarnPercent,
        double diskCriticalPercent
) {
    public static ThresholdSettings defaults() {
        return new ThresholdSettings(70.0, 90.0, 75.0, 90.0, 80.0, 95.0);
    }

    public ThresholdSettings {
        cpuWarnPercent = clamp(cpuWarnPercent);
        cpuCriticalPercent = clamp(cpuCriticalPercent);
        ramWarnPercent = clamp(ramWarnPercent);
        ramCriticalPercent = clamp(ramCriticalPercent);
        diskWarnPercent = clamp(diskWarnPercent);
        diskCriticalPercent = clamp(diskCriticalPercent);
    }

    private static double clamp(double value) {
        if (Double.isNaN(value) || value < 0.0) {
            return 0.0;
        }
        return Math.min(100.0, value);
    }
}
