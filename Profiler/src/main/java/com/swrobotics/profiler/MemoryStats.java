package com.swrobotics.profiler;

public final class MemoryStats {
    public static final long BYTES_PER_MB = 1024 * 1024;

    private final long free, max, total;

    public MemoryStats(long free, long max, long total) {
        this.free = free;
        this.max = max;
        this.total = total;
    }

    public long getUsed() {
        return total - free;
    }

    public long getFree() {
        return free;
    }

    public long getMax() {
        return max;
    }

    public long getTotal() {
        return total;
    }

    public static MemoryStats current() {
        Runtime rt = Runtime.getRuntime();
        return new MemoryStats(rt.freeMemory(), rt.maxMemory(), rt.totalMemory());
    }
}
