package com.swrobotics.lib.time;

/**
 * Helper class to run a function at a fixed interval.
 */
public final class Repeater {
    private final Duration interval;
    private final Runnable tickFn;
    private Timestamp lastTickTime;

    /**
     * Creates a new repeater which runs a function at a given interval.
     * 
     * @param interval interval between successive runs
     * @param tickFn function to run
     */
    public Repeater(Duration interval, Runnable tickFn) {
        this.interval = interval;
        this.tickFn = tickFn;
        lastTickTime = null;
    }

    /**
     * Ticks the timer, and runs the tick function if necessary.
     */
    public void tick() {
        if (lastTickTime == null)
            lastTickTime = Timestamp.now();

        Timestamp now = Timestamp.now();
        double nanoDiff = now.difference(lastTickTime).getDurationNanos();
        double nanoInt = interval.getDurationNanos();

        while (nanoDiff > nanoInt) {
            tickFn.run();
            lastTickTime = lastTickTime.addNanos(nanoInt);
            nanoDiff -= nanoInt;
        }
    }
}
