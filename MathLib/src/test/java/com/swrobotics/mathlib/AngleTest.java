package com.swrobotics.mathlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class AngleTest {
    @Test
    public void test_zero() {
        assertEquals(Angle.ZERO.ccw().rad(), 0, 0.0001);
    }
}
