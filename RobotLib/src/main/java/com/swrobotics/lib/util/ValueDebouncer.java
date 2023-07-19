package com.swrobotics.lib.util;

import com.swrobotics.lib.time.Duration;
import com.swrobotics.lib.time.Timestamp;

/**
 * Debounces an input value by keeping a value steady until a new value is
 * sustained.
 *
 * @param <T> type of value to debounce
 */
public final class ValueDebouncer<T> {
    private final Duration time;
    private T currentVal;
    private T futureVal;
    private Timestamp futureStartTime;

    /**
     * Creates a new instance with specified debounce time and initial value.
     *
     * @param time time a new value must exist to become current
     * @param initialVal initial value
     */
    public ValueDebouncer(Duration time, T initialVal) {
        this.time = time;
        currentVal = futureVal = initialVal;
        futureStartTime = Timestamp.now();
    }

    /**
     * Debounces the input value.
     *
     * @param input value calculated this periodic
     * @return debounced value
     */
    public T debounce(T input) {
        Timestamp now = Timestamp.now();
        if (input != futureVal) {
            futureVal = input;
            futureStartTime = now;
        }

        if (now.durationSince(futureStartTime).isLongerThan(time)) {
            currentVal = input;
        }

        return currentVal;
    }
}
