package com.houdini.monitor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fixed-capacity ring buffer. Oldest elements are dropped when full.
 * Thread-safe for concurrent writers/readers.
 *
 * @param <T> element type
 */
public final class CircularBuffer<T> {

    private final Object[] elements;
    private final int capacity;
    private int head;
    private int size;

    public CircularBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        this.capacity = capacity;
        this.elements = new Object[capacity];
    }

    public int capacity() {
        return capacity;
    }

    public synchronized int size() {
        return size;
    }

    public synchronized boolean isEmpty() {
        return size == 0;
    }

    public synchronized void clear() {
        for (int i = 0; i < capacity; i++) {
            elements[i] = null;
        }
        head = 0;
        size = 0;
    }

    public synchronized void add(T value) {
        Objects.requireNonNull(value, "value");
        int index = (head + size) % capacity;
        if (size == capacity) {
            elements[head] = value;
            head = (head + 1) % capacity;
        } else {
            elements[index] = value;
            size++;
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized List<T> snapshot() {
        List<T> copy = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            copy.add((T) elements[(head + i) % capacity]);
        }
        return List.copyOf(copy);
    }

    @SuppressWarnings("unchecked")
    public synchronized T newest() {
        if (size == 0) {
            return null;
        }
        int index = (head + size - 1) % capacity;
        return (T) elements[index];
    }

    @SuppressWarnings("unchecked")
    public synchronized T oldest() {
        if (size == 0) {
            return null;
        }
        return (T) elements[head];
    }
}
