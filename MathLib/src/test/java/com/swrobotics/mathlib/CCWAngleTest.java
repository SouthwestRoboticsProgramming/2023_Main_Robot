package com.swrobotics.mathlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class CCWAngleTest extends AbstractAngleTest<CCWAngle> {
    @Override
    protected CCWAngle create(double rad) {
        return CCWAngle.rad(rad);
    }

    @Test
    public void test_cw() {
        assertEquals(CCWAngle.rad(2).cw().rad(), -2, 0.0001);
        assertEquals(CCWAngle.rad(-12).cw().rad(), 12, 0.0001);
        assertEquals(CCWAngle.rad(45).cw().rad(), -45, 0.0001);
    }

    @Test
    public void test_ccw() {
        assertEquals(CCWAngle.rad(2).ccw().rad(), 2, 0.0001);
        assertEquals(CCWAngle.rad(-12).ccw().rad(), -12, 0.0001);
        assertEquals(CCWAngle.rad(45).ccw().rad(), 45, 0.0001);
    }

    @Test
    public void test_abs() {
        assertEquals(CCWAngle.rad(2).abs().rad(), 2, 0.0001);
        assertEquals(CCWAngle.rad(-12).abs().rad(), 12, 0.0001);
        assertEquals(CCWAngle.rad(45).abs().rad(), 45, 0.0001);
    }
}
