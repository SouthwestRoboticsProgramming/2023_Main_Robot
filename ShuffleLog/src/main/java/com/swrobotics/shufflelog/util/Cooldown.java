package com.swrobotics.shufflelog.util;

public final class Cooldown {
    private final long minInterval;
    private long lastRunTime;

    public Cooldown(long minInterval) {
        this.minInterval = minInterval;
        lastRunTime = System.nanoTime() - minInterval; // Guarantee that the first time will succeed
    }

    public boolean request() {
        long time = System.nanoTime();
        if (time - lastRunTime > minInterval) {
            lastRunTime = time;
            return true;
        } else {
            return false;
        }
    }
}
