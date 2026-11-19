package com.houdini.monitor.domain;

import java.util.Objects;
import java.util.Optional;

public final class GpuSnapshot {
    private final boolean available;
    private final String name;
    private final Double usagePercent;
    private final Long memoryUsedBytes;
    private final Long memoryTotalBytes;

    private GpuSnapshot(
            boolean available,
            String name,
            Double usagePercent,
            Long memoryUsedBytes,
            Long memoryTotalBytes
    ) {
        this.available = available;
        this.name = Objects.requireNonNullElse(name, "");
        this.usagePercent = usagePercent;
        this.memoryUsedBytes = memoryUsedBytes;
        this.memoryTotalBytes = memoryTotalBytes;
    }

    public static GpuSnapshot unavailable() {
        return new GpuSnapshot(false, "", null, null, null);
    }

    public static GpuSnapshot of(
            String name,
            Double usagePercent,
            Long memoryUsedBytes,
            Long memoryTotalBytes
    ) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasUsage = usagePercent != null;
        boolean hasMem = memoryTotalBytes != null && memoryTotalBytes > 0;
        if (!hasName && !hasUsage && !hasMem) {
            return unavailable();
        }
        return new GpuSnapshot(true, hasName ? name.trim() : "", usagePercent, memoryUsedBytes, memoryTotalBytes);
    }

    public boolean available() {
        return available;
    }

    public String name() {
        return name;
    }

    public Optional<Double> usagePercent() {
        return Optional.ofNullable(usagePercent);
    }

    public Optional<Long> memoryUsedBytes() {
        return Optional.ofNullable(memoryUsedBytes);
    }

    public Optional<Long> memoryTotalBytes() {
        return Optional.ofNullable(memoryTotalBytes);
    }
}
