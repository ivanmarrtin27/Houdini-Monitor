package com.houdini.monitor.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OshiMetricsCollectorNetworkDisplayTest {

    @Test
    void mapsSoftCapThroughputToPercent() {
        assertEquals(0.0, OshiMetricsCollector.networkDisplayPercent(0, 0), 0.001);
        double half = OshiMetricsCollector.networkDisplayPercent(50 * 1024 * 1024, 0);
        assertEquals(50.0, half, 0.1);
        assertEquals(100.0, OshiMetricsCollector.networkDisplayPercent(200 * 1024 * 1024, 0), 0.001);
        assertTrue(OshiMetricsCollector.networkDisplayPercent(-1, -1) >= 0.0);
    }
}
