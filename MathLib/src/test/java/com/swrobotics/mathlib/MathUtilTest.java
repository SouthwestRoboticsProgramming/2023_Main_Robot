package com.swrobotics.mathlib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public final class MathUtilTest {
    @Test
    public void test_clamp() {
        assertEquals(MathUtil.clamp(-1, 2, 4), 2, 0.0001);
        assertEquals(MathUtil.clamp(3, -10, 49), 3, 0.0001);
        assertEquals(MathUtil.clamp(94, -10, -8), -8, 0.0001);

        assertThrows(IllegalArgumentException.class, () -> MathUtil.clamp(0, 1, 0));
    }

    @Test
    public void test_lerp() {
        assertEquals(MathUtil.lerp(1, 2, 0.5), 1.5, 0.0001);
        assertEquals(MathUtil.lerp(-1, 1, 0.5), 0, 0.0001);
        assertEquals(MathUtil.lerp(-5, -1, 0.75), -2, 0.0001);
    }

    @Test
    public void test_percent() {
        assertEquals(MathUtil.percent(0, 0, 1), 0, 0.0001);
        assertEquals(MathUtil.percent(4, 2, 6), 0.5, 0.0001);
        assertEquals(MathUtil.percent(5, 1, 2), 4, 0.0001);
	assertEquals(MathUtil.percent(0, -5, 5), 0.5, 0.0001);
	assertEquals(MathUtil.percent(-3, -4, -2), 0.5, 0.0001);
	assertEquals(MathUtil.percent(-4, -4, -2), 0, 0.0001);
    }

    @Test
    public void test_map() {
        assertEquals(MathUtil.map(0, -1, 1, 4, 14), 9, 0.0001);
        assertEquals(MathUtil.map(10, 5, 25, 0, 1), 0.25, 0.0001);
        assertEquals(MathUtil.map(1, 0, 1, 1, 2), 2, 0.0001);
    }

    @Test
    public void test_floorMod() {
        assertEquals(MathUtil.floorMod(0, 1), 0, 0.0001);
        assertEquals(MathUtil.floorMod(3, 1), 0, 0.0001);
        assertEquals(MathUtil.floorMod(27, 10), 7, 0.0001);
        assertEquals(MathUtil.floorMod(-3, 10), 7, 0.0001);
        assertEquals(MathUtil.floorMod(-19, 20), 1, 0.0001);

        assertThrows(ArithmeticException.class, () -> MathUtil.floorMod(1, 0));
    }

    @Test
    public void test_wrap() {
        assertEquals(MathUtil.wrap(0, -1, 1), 0, 0.0001);
        assertEquals(MathUtil.wrap(-0.5, 0, 1), 0.5, 0.0001);
        assertEquals(MathUtil.wrap(-27, -100, 100), -27, 0.0001);
        assertEquals(MathUtil.wrap(27, -100, 100), 27, 0.0001);
        assertEquals(MathUtil.wrap(-127, -100, 100), 73, 0.0001);
        assertEquals(MathUtil.wrap(127, -100, 100), -73, 0.0001);

        assertThrows(IllegalArgumentException.class, () -> MathUtil.wrap(0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> MathUtil.wrap(0, 84, 8));
    }

    @Test
    public void test_deadband() {
        assertEquals(MathUtil.deadband(0, 1), 0, 0.0001);
        assertEquals(MathUtil.deadband(0.5, 1), 0, 0.0001);
        assertEquals(MathUtil.deadband(-0.5, 1), 0, 0.0001);
        assertEquals(MathUtil.deadband(0.75, 0.5), 0.5, 0.0001);
        assertEquals(MathUtil.deadband(-0.625, 0.5), -0.25, 0.0001);

        assertThrows(IllegalArgumentException.class, () -> MathUtil.deadband(0, -1));
    }
}
