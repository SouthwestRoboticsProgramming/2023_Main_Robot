package com.swrobotics.shufflelog.util;

public final class SmoothFloat {
    private final float percent;

    private long lastStepTime;
    private float value;
    private float targetValue;

    public SmoothFloat(float percent) {
        this(percent, 0);
    }

    public SmoothFloat(float percent, float value) {
        this.percent = percent;
        this.value = value;
        targetValue = value;
        lastStepTime = System.nanoTime();
    }

    public void step() {
        // Calculate time delta
        long time = System.nanoTime();
        float delta = (time - lastStepTime) / 1_000_000_000.0f;
        lastStepTime = time;

        // Step
        float oldVal = value;
        float diff = targetValue - value;
        value += delta * percent * diff;

        // Clamp
        float min = Math.min(oldVal, targetValue);
        float max = Math.max(oldVal, targetValue);
        if (value < min) value = min;
        if (value > max) value = max;
    }

    public float get() {
        return value;
    }

    public float getTarget() {
        return targetValue;
    }

    public void set(float value) {
        targetValue = value;
    }

    public void setNow(float value) {
        this.value = value;
        targetValue = value;
    }
}
