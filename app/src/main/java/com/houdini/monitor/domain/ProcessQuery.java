package com.houdini.monitor.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ProcessQuery {

    public enum SortColumn {
        NAME,
        PID,
        CPU,
        RAM,
        USER
    }

    private ProcessQuery() {
    }

    public static List<ProcessInfo> filterAndSort(
            List<ProcessInfo> processes,
            String searchText,
            SortColumn sortColumn,
            boolean ascending
    ) {
        Objects.requireNonNull(processes, "processes");
        Objects.requireNonNull(sortColumn, "sortColumn");
        String needle = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ROOT);
        List<ProcessInfo> filtered = new ArrayList<>();
        for (ProcessInfo process : processes) {
            if (matches(process, needle)) {
                filtered.add(process);
            }
        }
        Comparator<ProcessInfo> comparator = comparatorFor(sortColumn);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        filtered.sort(comparator);
        return List.copyOf(filtered);
    }

    private static boolean matches(ProcessInfo process, String needle) {
        if (needle.isEmpty()) {
            return true;
        }
        if (process.name().toLowerCase(Locale.ROOT).contains(needle)) {
            return true;
        }
        if (Long.toString(process.pid()).contains(needle)) {
            return true;
        }
        return process.user().toLowerCase(Locale.ROOT).contains(needle);
    }

    private static Comparator<ProcessInfo> comparatorFor(SortColumn column) {
        return switch (column) {
            case NAME -> Comparator.comparing(p -> p.name().toLowerCase(Locale.ROOT));
            case PID -> Comparator.comparingLong(ProcessInfo::pid);
            case CPU -> Comparator.comparingDouble(ProcessInfo::cpuPercent);
            case RAM -> Comparator.comparingLong(ProcessInfo::memoryBytes);
            case USER -> Comparator.comparing(p -> p.user().toLowerCase(Locale.ROOT));
        };
    }
}
