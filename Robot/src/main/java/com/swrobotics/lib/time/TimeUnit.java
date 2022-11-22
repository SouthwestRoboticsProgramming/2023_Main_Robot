package com.swrobotics.lib.time;

/**
 * Represents a unit that time can be measured in.
 */
public enum TimeUnit {
    NANOSECONDS (1),
    MICROSECONDS(1_000),
    MILLISECONDS(1_000_000),
    SECONDS     (1_000_000_000),
    MINUTES     (60_000_000_000L),
    HOURS       (3_600_000_000_000L);
    // We likely won't need any units higher than this

    private final long durationNanos;

    TimeUnit(long durationNanos) {
        this.durationNanos = durationNanos;
    }

    public long getDurationNanos() {
        return durationNanos;
    }
}
