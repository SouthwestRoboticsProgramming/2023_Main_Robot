package com.swrobotics.lib.time;

import java.util.Objects;

/** Represents a span of time. */
public final class Duration {
    private final double count;
    private final TimeUnit unit;

    /**
     * Creates a new duration with a specified amount of time
     *
     * @param count amount of time
     * @param unit unit the amount is in
     */
    public Duration(double count, TimeUnit unit) {
        this.count = count;
        this.unit = unit;
    }

    /**
     * Gets the amount of time represented in a specified unit.
     *
     * @param unit unit to get time in
     * @return amount of time
     */
    public double getCount(TimeUnit unit) {
        return count * this.unit.getDurationNanos() / unit.getDurationNanos();
    }

    /**
     * Gets the amount of time this duration represents in nanoseconds.
     *
     * @return time in nanoseconds
     */
    public double getDurationNanos() {
        return getCount(TimeUnit.NANOSECONDS);
    }

    public boolean isLongerThan(Duration o) {
        return getDurationNanos() > o.getDurationNanos();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!o.getClass().equals(getClass())) return false;

        Duration dur = (Duration) o;
        return dur.count == count && dur.unit == unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, unit);
    }
}
