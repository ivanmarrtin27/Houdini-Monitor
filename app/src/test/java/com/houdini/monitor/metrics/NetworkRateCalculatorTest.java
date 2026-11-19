package com.houdini.monitor.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetworkRateCalculatorTest {

    @Test
    void computesBytesPerSecond() {
        assertEquals(1000.0, NetworkRateCalculator.bytesPerSecond(0, 1000, 1000), 0.001);
        assertEquals(2000.0, NetworkRateCalculator.bytesPerSecond(1000, 3000, 1000), 0.001);
    }

    @Test
    void returnsZeroOnInvalidElapsedOrReset() {
        assertEquals(0.0, NetworkRateCalculator.bytesPerSecond(0, 100, 0));
        assertEquals(0.0, NetworkRateCalculator.bytesPerSecond(0, 100, -5));
        assertEquals(0.0, NetworkRateCalculator.bytesPerSecond(500, 100, 1000));
    }
}
