package com.houdini.monitor.domain;

import java.util.Objects;

public final class NetworkInterfaceSnapshot {
    private final String name;
    private final double bytesPerSecIn;
    private final double bytesPerSecOut;
    private final String ipv4;

    public NetworkInterfaceSnapshot(String name, double bytesPerSecIn, double bytesPerSecOut, String ipv4) {
        this.name = Objects.requireNonNullElse(name, "");
        this.bytesPerSecIn = Math.max(0.0, bytesPerSecIn);
        this.bytesPerSecOut = Math.max(0.0, bytesPerSecOut);
        this.ipv4 = Objects.requireNonNullElse(ipv4, "");
    }

    public String name() {
        return name;
    }

    public double bytesPerSecIn() {
        return bytesPerSecIn;
    }

    public double bytesPerSecOut() {
        return bytesPerSecOut;
    }

    public String ipv4() {
        return ipv4;
    }
}
