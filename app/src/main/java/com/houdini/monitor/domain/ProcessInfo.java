package com.houdini.monitor.domain;

import java.util.Objects;

public final class ProcessInfo {
    private final String name;
    private final long pid;
    private final double cpuPercent;
    private final long memoryBytes;
    private final String user;
    private final boolean systemLooking;

    public ProcessInfo(
            String name,
            long pid,
            double cpuPercent,
            long memoryBytes,
            String user,
            boolean systemLooking
    ) {
        if (pid <= 0) {
            throw new IllegalArgumentException("pid must be > 0");
        }
        this.name = Objects.requireNonNullElse(name, "");
        this.pid = pid;
        this.cpuPercent = Math.max(0.0, cpuPercent);
        this.memoryBytes = Math.max(0L, memoryBytes);
        this.user = user == null ? "" : user;
        this.systemLooking = systemLooking;
    }

    public String name() {
        return name;
    }

    public long pid() {
        return pid;
    }

    public double cpuPercent() {
        return cpuPercent;
    }

    public long memoryBytes() {
        return memoryBytes;
    }

    public String user() {
        return user;
    }

    public boolean systemLooking() {
        return systemLooking;
    }
}
