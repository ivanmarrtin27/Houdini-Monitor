package com.houdini.monitor.metrics;

public final class NetworkRateCalculator {

    private NetworkRateCalculator() {
    }

    /**
     * Bytes per second between two cumulative counters.
     * Returns 0 when elapsed is non-positive or counters decreased (reset).
     */
    public static double bytesPerSecond(long previousBytes, long currentBytes, long elapsedMillis) {
        if (elapsedMillis <= 0L) {
            return 0.0;
        }
        if (currentBytes < previousBytes) {
            return 0.0;
        }
        long delta = currentBytes - previousBytes;
        return delta * 1000.0 / elapsedMillis;
    }
}
