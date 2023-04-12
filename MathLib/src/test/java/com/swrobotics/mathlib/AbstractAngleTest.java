package com.swrobotics.mathlib;

import static org.junit.Assert.*;

import org.junit.Test;

public abstract class AbstractAngleTest<T extends AbstractAngle<T>> {
    protected abstract T create(double rad);

    @Test
    public void test_rad() {
        assertEquals(create(0).rad(), 0, 0.0001);
        assertEquals(create(763).rad(), 763, 0.0001);
        assertEquals(create(-1289347).rad(), -1289347, 0.0001);
    }

    @Test
    public void test_deg() {
        assertEquals(create(0).deg(), 0, 0.0001);
        assertEquals(create(Math.PI).deg(), 180, 0.0001);
        assertEquals(create(-Math.PI / 2).deg(), -90, 0.0001);
    }

    @Test
    public void test_rot() {
        assertEquals(create(0).rot(), 0, 0.0001);
        assertEquals(create(Math.PI * 74).rot(), 37, 0.0001);
        assertEquals(create(Math.PI * -23).rot(), -11.5, 0.0001);
    }

    @Test
    public void test_add() {
        assertEquals(create(13).add(create(15)).rad(), 13 + 15, 0.0001);
        assertEquals(create(831).add(create(-738)).rad(), 831 - 738, 0.0001);
        assertEquals(create(-183.14).add(create(7234)).rad(), -183.14 + 7234, 0.0001);
    }

    @Test
    public void test_sub() {
        assertEquals(create(13).sub(create(15)).rad(), 13 - 15, 0.0001);
        assertEquals(create(831).sub(create(-738)).rad(), 831 + 738, 0.0001);
        assertEquals(create(-183.14).sub(create(7234)).rad(), -183.14 - 7234, 0.0001);
    }

    @Test
    public void test_mul() {
        assertEquals(create(13).mul(84).rad(), 13 * 84, 0.0001);
        assertEquals(create(7418).mul(0.01).rad(), 74.18, 0.0001);
        assertEquals(create(999).mul(-10).rad(), -9990, 0.0001);
    }

    @Test
    public void test_div() {
        assertEquals(create(38).div(2).rad(), 19, 0.0001);
        assertEquals(create(847371).div(473).rad(), 847371 / 473.0, 0.0001);
        assertEquals(create(0.01).div(0.01).rad(), 1, 0.0001);
    }

    interface WrapMinMaxFn {
        void testWrap(double in, double min, double max, double expect);
    }

    private void wrapTest(WrapMinMaxFn fn) {
        fn.testWrap(0, -1, 1, 0);
        fn.testWrap(-0.5, 0, 1, 0.5);
        fn.testWrap(-27, -100, 100, -27);
        fn.testWrap(27, -100, 100, 27);
        fn.testWrap(-127, -100, 100, 73);
        fn.testWrap(127, -100, 100, -73);
    }

    interface WrapRangeFn {
        void testWrap(double in, double range, double expect);
    }

    private void wrapTest(WrapRangeFn fn) {
        fn.testWrap(0, 1, 0);
        fn.testWrap(-27, 100, -27);
        fn.testWrap(27, 100, 27);
        fn.testWrap(-127, 100, 73);
        fn.testWrap(127, 100, -73);
    }

    @Test
    public void test_wrapRad_minMax() {
        wrapTest(
                (val, min, max, expect) ->
                        assertEquals(create(val).wrapRad(min, max).rad(), expect, 0.0001));
    }

    @Test
    public void test_wrapDeg_minMax() {
        wrapTest(
                (val, min, max, expect) ->
                        assertEquals(
                                create(Math.toRadians(val)).wrapDeg(min, max).deg(),
                                expect,
                                0.0001));
    }

    @Test
    public void test_wrapRot_minMax() {
        wrapTest(
                (val, min, max, expect) ->
                        assertEquals(
                                create(val * Math.PI * 2).wrapRot(min, max).rot(), expect, 0.0001));
    }

    @Test
    public void test_wrapRad_range() {
        wrapTest(
                (val, range, expect) ->
                        assertEquals(create(val).wrapRad(range).rad(), expect, 0.0001));
    }

    @Test
    public void test_wrapDeg_range() {
        wrapTest(
                (val, range, expect) ->
                        assertEquals(
                                create(Math.toRadians(val)).wrapDeg(range).deg(), expect, 0.0001));
    }

    @Test
    public void test_wrapRot_range() {
        wrapTest(
                (val, range, expect) ->
                        assertEquals(
                                create(val * Math.PI * 2).wrapRot(range).rot(), expect, 0.0001));
    }

    @Test
    public void test_getAbsDiff() {
        assertEquals(create(0).getAbsDiff(create(0)).rad(), 0, 0.0001);
        assertEquals(create(1).getAbsDiff(create(0.93)).rad(), 0.07, 0.0001);
        assertEquals(create(0.92).getAbsDiff(create(1)).rad(), 0.08, 0.0001);
        assertEquals(create(0.93 + Math.PI * 2).getAbsDiff(create(1)).rad(), 0.07, 0.0001);
    }

    @Test
    public void test_inTolerance() {
        assertTrue(create(0).inTolerance(create(0), create(1)));
        assertTrue(create(0.2).inTolerance(create(0.3), create(0.2)));
        assertTrue(create(0.2).inTolerance(create(0.1), create(0.2)));
        assertFalse(create(0.2).inTolerance(create(0.4), create(0.1)));
        assertFalse(create(0.2).inTolerance(create(0), create(0.1)));
    }

    @Test
    public void test_negate() {
        assertEquals(create(0).negate().rad(), 0, 0.0001);
        assertEquals(create(10).negate().rad(), -10, 0.0001);
        assertEquals(create(-283).negate().rad(), 283, 0.0001);
    }

    @Test
    public void test_sin() {
        assertEquals(create(0).sin(), 0, 0.0001);
        assertEquals(create(Math.PI / 2).sin(), 1, 0.0001);
        assertEquals(create(Math.PI).sin(), 0, 0.0001);
        assertEquals(create(3 * Math.PI / 2).sin(), -1, 0.0001);
    }

    @Test
    public void test_cos() {
        assertEquals(create(0).cos(), 1, 0.0001);
        assertEquals(create(Math.PI / 2).cos(), 0, 0.0001);
        assertEquals(create(Math.PI).cos(), -1, 0.0001);
        assertEquals(create(3 * Math.PI / 2).cos(), 0, 0.0001);
    }

    @Test
    public void test_equals() {
        T same = create(123);
        assertEquals(same, same);
        assertNotEquals(null, same);
        assertEquals(create(1), create(1));
        assertNotEquals(create(1), create(2));
        assertEquals(CCWAngle.rad(1), CWAngle.rad(-1));
    }

    @Test
    public void test_hashCode() {
        assertEquals(create(1).hashCode(), create(1).hashCode());
        assertEquals(create(2374).hashCode(), create(2374).hashCode());
    }
}
