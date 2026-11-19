package com.houdini.monitor.metrics;

import com.houdini.monitor.collector.MetricCollector;
import com.houdini.monitor.domain.CpuCoreSnapshot;
import com.houdini.monitor.domain.GpuSnapshot;
import com.houdini.monitor.domain.NetworkInterfaceSnapshot;
import com.houdini.monitor.domain.ProcessInfo;
import com.houdini.monitor.domain.ResourceKind;
import com.houdini.monitor.domain.ResourceSnapshot;
import com.houdini.monitor.domain.SystemMetricsSnapshot;
import com.houdini.monitor.domain.kill.KillConfirmationPolicy;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class OshiMetricsCollector implements MetricCollector<SystemMetricsSnapshot> {

    private final SystemInfo systemInfo = new SystemInfo();
    private final KillConfirmationPolicy killPolicy = new KillConfirmationPolicy();
    private long[] previousCpuTicks;
    private long[][] previousCoreTicks;
    private final Map<Integer, OSProcess> previousProcesses = new HashMap<>();
    private final Map<String, long[]> previousNetworkCounters = new HashMap<>();
    private long previousNetworkSampleEpochMs;

    @Override
    public SystemMetricsSnapshot collect() {
        long now = System.currentTimeMillis();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        OperatingSystem os = systemInfo.getOperatingSystem();
        CentralProcessor processor = hardware.getProcessor();

        double cpuPercent = sampleCpuPercent(processor);
        List<CpuCoreSnapshot> cores = sampleCpuCores(processor);

        GlobalMemory memory = hardware.getMemory();
        long total = memory.getTotal();
        long available = memory.getAvailable();
        double ramPercent = total <= 0 ? 0.0 : ((total - available) * 100.0) / total;

        List<DiskUsageAggregator.VolumeUsage> volumes = new ArrayList<>();
        List<ResourceSnapshot> volumeSnapshots = new ArrayList<>();
        for (OSFileStore store : os.getFileSystem().getFileStores()) {
            if (!isLocalFixedStore(store)) {
                continue;
            }
            long volTotal = store.getTotalSpace();
            long usable = store.getUsableSpace();
            long used = Math.max(0L, volTotal - usable);
            String label = store.getMount();
            volumes.add(new DiskUsageAggregator.VolumeUsage(label, used, volTotal));
            if (volTotal > 0) {
                volumeSnapshots.add(new ResourceSnapshot(
                        ResourceKind.DISK,
                        now,
                        (used * 100.0) / volTotal,
                        label
                ));
            }
        }
        double diskPercent = DiskUsageAggregator.aggregateUsedPercent(volumes);

        NetworkSample network = sampleNetwork(hardware.getNetworkIFs(false), now);
        double networkDisplay = networkDisplayPercent(network.bytesPerSecIn(), network.bytesPerSecOut());

        GpuSnapshot gpu = sampleGpu(hardware);
        double gpuPercent = gpu.usagePercent().orElse(0.0);

        List<ProcessInfo> processes = collectProcesses(os);

        long bootEpochMs = os.getSystemBootTime() * 1000L;
        long uptimeSeconds = Math.max(0L, os.getSystemUptime());

        return new SystemMetricsSnapshot(
                now,
                ResourceSnapshot.of(ResourceKind.CPU, now, cpuPercent),
                cores,
                ResourceSnapshot.of(ResourceKind.RAM, now, ramPercent),
                ResourceSnapshot.of(ResourceKind.DISK, now, diskPercent),
                volumeSnapshots,
                new ResourceSnapshot(ResourceKind.NETWORK, now, networkDisplay, "aggregate"),
                network.bytesPerSecIn(),
                network.bytesPerSecOut(),
                network.interfaces(),
                gpu,
                processes,
                bootEpochMs,
                uptimeSeconds
        );
    }

    private double sampleCpuPercent(CentralProcessor processor) {
        if (previousCpuTicks == null) {
            previousCpuTicks = processor.getSystemCpuLoadTicks();
            return 0.0;
        }
        double load = processor.getSystemCpuLoadBetweenTicks(previousCpuTicks) * 100.0;
        previousCpuTicks = processor.getSystemCpuLoadTicks();
        return load;
    }

    private List<CpuCoreSnapshot> sampleCpuCores(CentralProcessor processor) {
        long[][] current = processor.getProcessorCpuLoadTicks();
        if (previousCoreTicks == null || previousCoreTicks.length != current.length) {
            previousCoreTicks = current;
            List<CpuCoreSnapshot> zeros = new ArrayList<>(current.length);
            for (int i = 0; i < current.length; i++) {
                zeros.add(new CpuCoreSnapshot(i, 0.0));
            }
            return zeros;
        }
        double[] loads = processor.getProcessorCpuLoadBetweenTicks(previousCoreTicks);
        previousCoreTicks = current;
        List<CpuCoreSnapshot> cores = new ArrayList<>(loads.length);
        for (int i = 0; i < loads.length; i++) {
            cores.add(new CpuCoreSnapshot(i, loads[i] * 100.0));
        }
        return cores;
    }

    private NetworkSample sampleNetwork(List<NetworkIF> networkIfs, long now) {
        long elapsed = previousNetworkSampleEpochMs <= 0 ? 0 : now - previousNetworkSampleEpochMs;
        Map<String, long[]> nextCounters = new HashMap<>();
        List<NetworkInterfaceSnapshot> interfaces = new ArrayList<>();
        double totalIn = 0.0;
        double totalOut = 0.0;

        for (NetworkIF nif : networkIfs) {
            try {
                nif.updateAttributes();
            } catch (Exception ignored) {
                continue;
            }
            if (isLoopback(nif)) {
                continue;
            }
            String key = nif.getName() + "|" + nif.getMacaddr();
            long recv = nif.getBytesRecv();
            long sent = nif.getBytesSent();
            long[] previous = previousNetworkCounters.get(key);
            double inRate = 0.0;
            double outRate = 0.0;
            if (previous != null && elapsed > 0) {
                inRate = NetworkRateCalculator.bytesPerSecond(previous[0], recv, elapsed);
                outRate = NetworkRateCalculator.bytesPerSecond(previous[1], sent, elapsed);
            }
            nextCounters.put(key, new long[]{recv, sent});
            totalIn += inRate;
            totalOut += outRate;
            String ipv4 = firstIpv4(nif);
            interfaces.add(new NetworkInterfaceSnapshot(displayName(nif), inRate, outRate, ipv4));
        }

        previousNetworkCounters.clear();
        previousNetworkCounters.putAll(nextCounters);
        previousNetworkSampleEpochMs = now;

        interfaces.sort(Comparator
                .comparingDouble((NetworkInterfaceSnapshot n) -> n.bytesPerSecIn() + n.bytesPerSecOut())
                .reversed());
        return new NetworkSample(totalIn, totalOut, interfaces);
    }

    private static boolean isLoopback(NetworkIF nif) {
        String name = nif.getName() == null ? "" : nif.getName().toLowerCase(Locale.ROOT);
        String display = nif.getDisplayName() == null ? "" : nif.getDisplayName().toLowerCase(Locale.ROOT);
        if (name.contains("loopback") || display.contains("loopback")) {
            return true;
        }
        String[] addrs = nif.getIPv4addr();
        if (addrs != null) {
            for (String addr : addrs) {
                if ("127.0.0.1".equals(addr)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String displayName(NetworkIF nif) {
        if (nif.getDisplayName() != null && !nif.getDisplayName().isBlank()) {
            return nif.getDisplayName();
        }
        return nif.getName() == null ? "?" : nif.getName();
    }

    private static String firstIpv4(NetworkIF nif) {
        String[] addrs = nif.getIPv4addr();
        if (addrs == null || addrs.length == 0) {
            return "";
        }
        return addrs[0] == null ? "" : addrs[0];
    }

    /**
     * Map aggregate throughput to a 0–100 chart series using a soft cap (100 MB/s = 100%).
     */
    static double networkDisplayPercent(double bytesPerSecIn, double bytesPerSecOut) {
        double total = Math.max(0.0, bytesPerSecIn) + Math.max(0.0, bytesPerSecOut);
        double softCap = 100.0 * 1024.0 * 1024.0;
        return Math.min(100.0, (total / softCap) * 100.0);
    }

    private GpuSnapshot sampleGpu(HardwareAbstractionLayer hardware) {
        try {
            List<GraphicsCard> cards = hardware.getGraphicsCards();
            if (cards == null || cards.isEmpty()) {
                return GpuSnapshot.unavailable();
            }
            GraphicsCard primary = cards.getFirst();
            String name = primary.getName();
            long vram = primary.getVRam();
            Long total = vram > 0 ? vram : null;
            return GpuSnapshot.of(name, null, null, total);
        } catch (Throwable ignored) {
            return GpuSnapshot.unavailable();
        }
    }

    private List<ProcessInfo> collectProcesses(OperatingSystem os) {
        List<OSProcess> current = os.getProcesses(null, OperatingSystem.ProcessSorting.CPU_DESC, 0);
        List<ProcessInfo> result = new ArrayList<>(current.size());
        Map<Integer, OSProcess> nextPrevious = new HashMap<>();

        for (OSProcess process : current) {
            if (process.getProcessID() <= 0) {
                continue;
            }
            OSProcess prior = previousProcesses.get(process.getProcessID());
            double cpu = prior == null ? 0.0 : process.getProcessCpuLoadBetweenTicks(prior) * 100.0;
            String user = process.getUser() == null ? "" : process.getUser();
            String name = process.getName() == null ? "" : process.getName();
            ProcessInfo provisional = new ProcessInfo(
                    name,
                    process.getProcessID(),
                    cpu,
                    process.getResidentSetSize(),
                    user,
                    false
            );
            boolean systemLooking = killPolicy.isSystemLooking(provisional);
            result.add(new ProcessInfo(
                    name,
                    process.getProcessID(),
                    cpu,
                    process.getResidentSetSize(),
                    user,
                    systemLooking
            ));
            nextPrevious.put(process.getProcessID(), process);
        }
        previousProcesses.clear();
        previousProcesses.putAll(nextPrevious);
        result.sort(Comparator.comparingDouble(ProcessInfo::cpuPercent).reversed());
        return result;
    }

    private static boolean isLocalFixedStore(OSFileStore store) {
        String type = store.getType() == null ? "" : store.getType().toLowerCase(Locale.ROOT);
        String description = store.getDescription() == null ? "" : store.getDescription().toLowerCase(Locale.ROOT);
        if (description.contains("network") || type.contains("network") || type.contains("nfs") || type.contains("smb")) {
            return false;
        }
        if (description.contains("removable") || type.contains("removable")) {
            return false;
        }
        return description.contains("fixed")
                || description.contains("local")
                || type.contains("ntfs")
                || type.contains("fat")
                || type.contains("exfat")
                || type.contains("refs")
                || store.getTotalSpace() > 0;
    }

    private record NetworkSample(
            double bytesPerSecIn,
            double bytesPerSecOut,
            List<NetworkInterfaceSnapshot> interfaces
    ) {
    }
}
