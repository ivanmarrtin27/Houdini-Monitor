package com.houdini.monitor.domain;

import java.util.List;
import java.util.Objects;

public final class SystemMetricsSnapshot {
    private final long capturedAtEpochMs;
    private final ResourceSnapshot cpu;
    private final List<CpuCoreSnapshot> cpuCores;
    private final ResourceSnapshot ram;
    private final ResourceSnapshot disk;
    private final List<ResourceSnapshot> diskVolumes;
    private final ResourceSnapshot network;
    private final double networkBytesPerSecIn;
    private final double networkBytesPerSecOut;
    private final List<NetworkInterfaceSnapshot> networkInterfaces;
    private final GpuSnapshot gpu;
    private final List<ProcessInfo> processes;
    private final long systemBootTimeEpochMs;
    private final long systemUptimeSeconds;

    public SystemMetricsSnapshot(
            long capturedAtEpochMs,
            ResourceSnapshot cpu,
            List<CpuCoreSnapshot> cpuCores,
            ResourceSnapshot ram,
            ResourceSnapshot disk,
            List<ResourceSnapshot> diskVolumes,
            ResourceSnapshot network,
            double networkBytesPerSecIn,
            double networkBytesPerSecOut,
            List<NetworkInterfaceSnapshot> networkInterfaces,
            GpuSnapshot gpu,
            List<ProcessInfo> processes,
            long systemBootTimeEpochMs,
            long systemUptimeSeconds
    ) {
        this.capturedAtEpochMs = capturedAtEpochMs;
        this.cpu = Objects.requireNonNull(cpu, "cpu");
        this.cpuCores = List.copyOf(cpuCores == null ? List.of() : cpuCores);
        this.ram = Objects.requireNonNull(ram, "ram");
        this.disk = Objects.requireNonNull(disk, "disk");
        this.diskVolumes = List.copyOf(diskVolumes == null ? List.of() : diskVolumes);
        this.network = Objects.requireNonNull(network, "network");
        this.networkBytesPerSecIn = Math.max(0.0, networkBytesPerSecIn);
        this.networkBytesPerSecOut = Math.max(0.0, networkBytesPerSecOut);
        this.networkInterfaces = List.copyOf(networkInterfaces == null ? List.of() : networkInterfaces);
        this.gpu = Objects.requireNonNull(gpu, "gpu");
        this.processes = List.copyOf(processes == null ? List.of() : processes);
        this.systemBootTimeEpochMs = systemBootTimeEpochMs;
        this.systemUptimeSeconds = Math.max(0L, systemUptimeSeconds);
    }

    public long capturedAtEpochMs() {
        return capturedAtEpochMs;
    }

    public ResourceSnapshot cpu() {
        return cpu;
    }

    public List<CpuCoreSnapshot> cpuCores() {
        return cpuCores;
    }

    public ResourceSnapshot ram() {
        return ram;
    }

    public ResourceSnapshot disk() {
        return disk;
    }

    public List<ResourceSnapshot> diskVolumes() {
        return diskVolumes;
    }

    public ResourceSnapshot network() {
        return network;
    }

    public double networkBytesPerSecIn() {
        return networkBytesPerSecIn;
    }

    public double networkBytesPerSecOut() {
        return networkBytesPerSecOut;
    }

    public List<NetworkInterfaceSnapshot> networkInterfaces() {
        return networkInterfaces;
    }

    public GpuSnapshot gpu() {
        return gpu;
    }

    public List<ProcessInfo> processes() {
        return processes;
    }

    public long systemBootTimeEpochMs() {
        return systemBootTimeEpochMs;
    }

    public long systemUptimeSeconds() {
        return systemUptimeSeconds;
    }
}
