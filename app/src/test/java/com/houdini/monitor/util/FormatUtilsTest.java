package com.houdini.monitor.util;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormatUtilsTest {

    private static final Locale LOCALE = Locale.US;

    @Test
    void formatsBinaryBytes() {
        assertEquals("512 B", FormatUtils.bytes(512, LOCALE));
        assertEquals("1.00 KiB", FormatUtils.bytes(1024, LOCALE));
        assertEquals("1.50 MiB", FormatUtils.bytes(1_572_864L, LOCALE));
        assertTrue(FormatUtils.bytes(3L * 1024 * 1024 * 1024, LOCALE).contains("GiB"));
    }

    @Test
    void formatsByteAndBitRatesDistinctly() {
        assertEquals("1.00 KiB/s", FormatUtils.bytesPerSecond(1024.0, LOCALE));
        assertEquals("8.00 Kbit/s", FormatUtils.bytesPerSecondAsBits(1000.0, LOCALE));
        assertTrue(FormatUtils.bitsPerSecond(1_500_000.0, LOCALE).contains("Mbit/s"));
    }

    @Test
    void formatsTemperaturePercentDuration() {
        assertEquals("42 °C", FormatUtils.celsius(42.4, LOCALE));
        assertEquals("12.5%", FormatUtils.percent(12.5, LOCALE));
        assertEquals("13%", FormatUtils.percentWhole(12.6));
        assertEquals("1h 02m 03s", FormatUtils.duration(3723));
        assertEquals("2m 05s", FormatUtils.duration(125));
        assertEquals("9s", FormatUtils.duration(9));
    }
}
