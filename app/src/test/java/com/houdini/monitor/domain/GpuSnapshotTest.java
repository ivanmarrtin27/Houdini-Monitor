package com.houdini.monitor.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GpuSnapshotTest {

    @Test
    void unavailableWhenNoUsefulData() {
        assertFalse(GpuSnapshot.of(null, null, null, null).available());
        assertFalse(GpuSnapshot.of("  ", null, null, 0L).available());
        assertFalse(GpuSnapshot.unavailable().available());
    }

    @Test
    void availableWithNameOrMemoryOrUsage() {
        assertTrue(GpuSnapshot.of("NVIDIA", null, null, null).available());
        assertTrue(GpuSnapshot.of("", 12.5, null, null).available());
        assertTrue(GpuSnapshot.of("", null, 100L, 1024L).available());
    }
}
