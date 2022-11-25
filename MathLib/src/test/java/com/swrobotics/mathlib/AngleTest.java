package com.swrobotics.mathlib;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class AngleTest {
    @Test
    public void test_zero() {
        assertEquals(Angle.ZERO.ccw().rad(), 0, 0.0001);
    }
}
