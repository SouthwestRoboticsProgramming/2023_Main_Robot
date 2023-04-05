package com.swrobotics.shufflelog.util;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Represents a fixed-size history buffer. This buffer retains a fixed number of elements submitted
 * to it in order of insertion.
 *
 * @param <T> type of data to store
 */
public final class RollingBuffer<T> {
    private final T[] data;
    private int index;
    private int length;

    /**
     * Creates a new buffer with a specified capacity. If more elements than the capacity are
     * inserted, the oldest items will be discarded.
     *
     * @param size capacity of the buffer
     */
    public RollingBuffer(int size) {
        // Can't create a generic array directly
        @SuppressWarnings("unchecked")
        T[] data = (T[]) new Object[size];
        this.data = data;

        index = 0;
        length = 0;
    }

    /**
     * Inserts a new data element into this buffer. If the buffer is full, the oldest element is
     * removed.
     *
     * @param t element to insert
     */
    public void insert(T t) {
        data[index++] = t;

        // Wrap back around to the beginning
        if (index >= data.length) index -= data.length;

        // Keep track of filled data element count
        // This is needed when the buffer is not yet full
        if (length < data.length) length++;
    }

    /** Removes all data from this buffer. */
    public void clear() {
        // Release all references on data to not leak objects
        Arrays.fill(data, null);

        index = 0;
        length = 0;
    }

    /**
     * Calls the iterator function for each element in the buffer, in order of insertion.
     *
     * @param iteratorFn function to call for each element
     */
    public void forEach(Consumer<T> iteratorFn) {
        for (int i = 0; i < length; i++) {
            int index = (this.index + i) % length;
            iteratorFn.accept(data[index]);
        }
    }
}
