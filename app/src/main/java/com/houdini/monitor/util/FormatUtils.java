package com.houdini.monitor.util;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Locale-aware formatting for bytes, rates, temperatures, percentages and durations.
 */
public final class FormatUtils {

    private static final String[] BYTE_UNITS = {"B", "KiB", "MiB", "GiB", "TiB"};
    private static final String[] BIT_RATE_UNITS = {"bit/s", "Kbit/s", "Mbit/s", "Gbit/s", "Tbit/s"};
    private static final String[] BYTE_RATE_UNITS = {"B/s", "KiB/s", "MiB/s", "GiB/s", "TiB/s"};

    private FormatUtils() {
    }

    public static String bytes(long bytes) {
        return scaled(Math.max(0L, bytes), 1024.0, BYTE_UNITS, Locale.getDefault());
    }

    public static String bytes(long bytes, Locale locale) {
        return scaled(Math.max(0L, bytes), 1024.0, BYTE_UNITS, locale);
    }

    public static String bytesPerSecond(double bytesPerSec) {
        return scaled(Math.max(0.0, bytesPerSec), 1024.0, BYTE_RATE_UNITS, Locale.getDefault());
    }

    public static String bytesPerSecond(double bytesPerSec, Locale locale) {
        return scaled(Math.max(0.0, bytesPerSec), 1024.0, BYTE_RATE_UNITS, locale);
    }

    public static String bitsPerSecond(double bitsPerSec) {
        return scaled(Math.max(0.0, bitsPerSec), 1000.0, BIT_RATE_UNITS, Locale.getDefault());
    }

    public static String bitsPerSecond(double bitsPerSec, Locale locale) {
        return scaled(Math.max(0.0, bitsPerSec), 1000.0, BIT_RATE_UNITS, locale);
    }

    /** Converts bytes/s to a human bit/s label (×8). */
    public static String bytesPerSecondAsBits(double bytesPerSec) {
        return bitsPerSecond(bytesPerSec * 8.0);
    }

    public static String bytesPerSecondAsBits(double bytesPerSec, Locale locale) {
        return bitsPerSecond(bytesPerSec * 8.0, locale);
    }

    public static String celsius(double celsius) {
        return String.format(Locale.getDefault(), "%.0f °C", celsius);
    }

    public static String celsius(double celsius, Locale locale) {
        return String.format(locale, "%.0f °C", celsius);
    }

    public static String percent(double value) {
        return String.format(Locale.getDefault(), "%.1f%%", value);
    }

    public static String percent(double value, Locale locale) {
        return String.format(locale, "%.1f%%", value);
    }

    public static String percentWhole(double value) {
        return String.format(Locale.getDefault(), "%.0f%%", value);
    }

    public static String duration(long totalSeconds) {
        long safe = Math.max(0L, totalSeconds);
        long hours = TimeUnit.SECONDS.toHours(safe);
        long minutes = TimeUnit.SECONDS.toMinutes(safe) % 60;
        long seconds = safe % 60;
        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, seconds);
        }
        if (minutes > 0) {
            return String.format("%dm %02ds", minutes, seconds);
        }
        return String.format("%ds", seconds);
    }

    private static String scaled(double value, double base, String[] units, Locale locale) {
        if (value < base) {
            if (value == Math.rint(value) && value < 1000) {
                return String.format(locale, "%.0f %s", value, units[0]);
            }
            return String.format(locale, "%.1f %s", value, units[0]);
        }
        int unit = 0;
        double scaled = value;
        while (scaled >= base && unit < units.length - 1) {
            scaled /= base;
            unit++;
        }
        if (scaled >= 100.0) {
            return String.format(locale, "%.0f %s", scaled, units[unit]);
        }
        if (scaled >= 10.0) {
            return String.format(locale, "%.1f %s", scaled, units[unit]);
        }
        return String.format(locale, "%.2f %s", scaled, units[unit]);
    }
}
