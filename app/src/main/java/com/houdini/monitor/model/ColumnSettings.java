package com.houdini.monitor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Visible process-table columns (order preserved).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ColumnSettings(List<String> processColumns) {
    public static final List<String> DEFAULT_PROCESS_COLUMNS =
            List.of("name", "pid", "cpu", "ram", "user");

    public static ColumnSettings defaults() {
        return new ColumnSettings(DEFAULT_PROCESS_COLUMNS);
    }

    public ColumnSettings {
        if (processColumns == null || processColumns.isEmpty()) {
            processColumns = DEFAULT_PROCESS_COLUMNS;
        } else {
            processColumns = List.copyOf(processColumns);
        }
    }
}
