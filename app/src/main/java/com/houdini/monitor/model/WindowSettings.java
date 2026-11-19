package com.houdini.monitor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Persisted main-window geometry. Null x/y means "use platform default position".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WindowSettings(
        double width,
        double height,
        Double x,
        Double y,
        boolean maximized
) {
    public static final double DEFAULT_WIDTH = 1180.0;
    public static final double DEFAULT_HEIGHT = 720.0;

    public static WindowSettings defaults() {
        return new WindowSettings(DEFAULT_WIDTH, DEFAULT_HEIGHT, null, null, false);
    }

    public WindowSettings {
        width = width <= 0 ? DEFAULT_WIDTH : width;
        height = height <= 0 ? DEFAULT_HEIGHT : height;
        if (x != null && (x.isNaN() || x.isInfinite())) {
            x = null;
        }
        if (y != null && (y.isNaN() || y.isInfinite())) {
            y = null;
        }
    }

    public boolean hasPosition() {
        return x != null && y != null;
    }
}
