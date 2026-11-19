package com.houdini.monitor.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessQueryTest {

    private static ProcessInfo proc(String name, long pid, double cpu, long ram, String user) {
        return new ProcessInfo(name, pid, cpu, ram, user, false);
    }

    @Test
    void filtersByNamePidOrUserIgnoreCase() {
        List<ProcessInfo> input = List.of(
                proc("chrome.exe", 100, 10, 1000, "alice"),
                proc("java.exe", 200, 50, 2000, "bob"),
                proc("notepad.exe", 300, 1, 500, "Alice")
        );

        List<ProcessInfo> byName = ProcessQuery.filterAndSort(
                input, "CHROME", ProcessQuery.SortColumn.CPU, false);
        assertEquals(1, byName.size());
        assertEquals(100, byName.getFirst().pid());

        List<ProcessInfo> byPid = ProcessQuery.filterAndSort(
                input, "200", ProcessQuery.SortColumn.CPU, false);
        assertEquals(1, byPid.size());
        assertEquals("java.exe", byPid.getFirst().name());

        List<ProcessInfo> byUser = ProcessQuery.filterAndSort(
                input, "alice", ProcessQuery.SortColumn.PID, true);
        assertEquals(2, byUser.size());
        assertEquals(100, byUser.get(0).pid());
        assertEquals(300, byUser.get(1).pid());
    }

    @Test
    void sortsAscendingAndDescending() {
        List<ProcessInfo> input = List.of(
                proc("b", 2, 20, 200, "u"),
                proc("a", 1, 10, 100, "u"),
                proc("c", 3, 30, 300, "u")
        );
        List<ProcessInfo> byCpuDesc = ProcessQuery.filterAndSort(
                input, "", ProcessQuery.SortColumn.CPU, false);
        assertEquals(List.of(3L, 2L, 1L), byCpuDesc.stream().map(ProcessInfo::pid).toList());

        List<ProcessInfo> byNameAsc = ProcessQuery.filterAndSort(
                input, "", ProcessQuery.SortColumn.NAME, true);
        assertEquals(List.of("a", "b", "c"), byNameAsc.stream().map(ProcessInfo::name).toList());
    }

    @Test
    void emptySearchReturnsAllSorted() {
        List<ProcessInfo> input = List.of(
                proc("z", 9, 1, 1, "u"),
                proc("a", 1, 9, 9, "u")
        );
        List<ProcessInfo> result = ProcessQuery.filterAndSort(
                input, "  ", ProcessQuery.SortColumn.RAM, false);
        assertEquals(2, result.size());
        assertEquals(1, result.getFirst().pid());
    }

    @Test
    void noMatchYieldsEmpty() {
        List<ProcessInfo> input = List.of(proc("x", 1, 1, 1, "u"));
        assertTrue(ProcessQuery.filterAndSort(
                input, "missing", ProcessQuery.SortColumn.NAME, true).isEmpty());
    }
}
