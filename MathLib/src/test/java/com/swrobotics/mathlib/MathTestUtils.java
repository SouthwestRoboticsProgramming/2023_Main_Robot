package com.swrobotics.mathlib;

import static org.junit.Assert.assertEquals;

public final class MathTestUtils {
    public static void assertFuzzyEquals(Vec2d a, Vec2d b, double tol) {
        assertEquals(a.x, b.x, tol);
        assertEquals(a.y, b.y, tol);
    }

    private MathTestUtils() {
        throw new AssertionError();
    }
}
