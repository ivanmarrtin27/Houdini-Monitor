package com.houdini.monitor.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircularBufferTest {

    @Test
    void dropsOldestWhenFull() {
        CircularBuffer<Integer> buffer = new CircularBuffer<>(3);
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);
        buffer.add(4);

        assertEquals(List.of(2, 3, 4), buffer.snapshot());
        assertEquals(2, buffer.oldest());
        assertEquals(4, buffer.newest());
    }

    @Test
    void rejectsInvalidCapacityAndNull() {
        assertThrows(IllegalArgumentException.class, () -> new CircularBuffer<>(0));
        CircularBuffer<String> buffer = new CircularBuffer<>(2);
        assertThrows(NullPointerException.class, () -> buffer.add(null));
    }

    @Test
    void clearResetsState() {
        CircularBuffer<String> buffer = new CircularBuffer<>(2);
        buffer.add("a");
        buffer.clear();
        assertTrue(buffer.isEmpty());
        assertNull(buffer.newest());
    }

    @Test
    void worksWithInjectableClockTimestamps() {
        AtomicLong now = new AtomicLong(1_000L);
        Clock clock = now::get;
        CircularBuffer<Long> buffer = new CircularBuffer<>(2);
        buffer.add(clock.millis());
        now.set(2_000L);
        buffer.add(clock.millis());
        assertEquals(List.of(1_000L, 2_000L), buffer.snapshot());
    }
}
