package com.swrobotics.shufflelog.math;

// TODO: Use lookup table for trig
public final class MathUtils {
    public static final float fPI = (float) Math.PI;

    public static float sqrtf(float value) {
        return (float) Math.sqrt(value);
    }

    public static float sinf(float value) {
        return (float) Math.sin(value);
    }

    public static float cosf(float value) {
        return (float) Math.cos(value);
    }

    public static float tanf(float value) {
        return (float) Math.tan(value);
    }

    public static float lerp(float a, float b, float f) {
        return a + (b - a) * f;
    }

    public static float map(float v, float m1, float n1, float m2, float n2) {
        return m2 + (n2 - m2) * (v - m1) / (n1 - m1);
    }

    public static int clamp(int v, int min, int max) {
        if (v < min) return min;
        return Math.min(v, max);
    }

    public static float clamp(float v, float min, float max) {
        if (v < min) return min;
        return Math.min(v, max);
    }

    private MathUtils() {
        throw new AssertionError();
    }
}
