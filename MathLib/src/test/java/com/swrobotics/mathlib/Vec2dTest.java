package com.swrobotics.mathlib;

import static org.junit.Assert.*;

import org.junit.Test;

public final class Vec2dTest {
    private void assertVecEquals(Vec2d vec, double x, double y) {
        assertEquals(vec.x, x, 0.0001);
        assertEquals(vec.y, y, 0.0001);
    }

    @Test
    public void test_constructor_noArgs() {
        assertVecEquals(new Vec2d(), 0, 0);
    }

    @Test
    public void test_constructor_components() {
        assertVecEquals(new Vec2d(1, 2), 1, 2);
        assertVecEquals(new Vec2d(-123, 148), -123, 148);
        assertVecEquals(new Vec2d(49.12, 8421.4), 49.12, 8421.4);
    }

    @Test
    public void test_constructor_copy() {
        assertVecEquals(new Vec2d(new Vec2d(1, 2)), 1, 2);
        assertVecEquals(new Vec2d(new Vec2d(-123, 148)), -123, 148);
        assertVecEquals(new Vec2d(new Vec2d(49.12, 8421.4)), 49.12, 8421.4);
    }

    @Test
    public void test_constructor_angleMag() {
        assertVecEquals(new Vec2d(Angle.ZERO, 1), 1, 0);
        assertVecEquals(new Vec2d(CCWAngle.deg(90), 2), 0, 2);
        assertVecEquals(new Vec2d(CCWAngle.deg(180), -4), 4, 0);
        assertVecEquals(new Vec2d(CCWAngle.deg(270), 0.2), 0, -0.2);
    }

    @Test
    public void test_setX() {
        Vec2d vec = new Vec2d(1, 0);
        assertSame(vec.setX(2), vec);
        assertEquals(vec.x, 2, 0.0001);
        assertSame(vec.setX(-13), vec);
        assertEquals(vec.x, -13, 0.0001);
    }

    @Test
    public void test_setY() {
        Vec2d vec = new Vec2d(0, 1);
        assertSame(vec.setY(2), vec);
        assertEquals(vec.y, 2, 0.0001);
        assertSame(vec.setY(-13), vec);
        assertEquals(vec.y, -13, 0.0001);
    }

    @Test
    public void test_set_components() {
        Vec2d vec = new Vec2d(1, 1);
        assertSame(vec.set(2, -13), vec);
        assertVecEquals(vec, 2, -13);
        assertSame(vec.set(-13, 2), vec);
        assertVecEquals(vec, -13, 2);
    }

    @Test
    public void test_set_copy() {
        Vec2d vec = new Vec2d(1, 1);
        assertSame(vec.set(new Vec2d(2, -13)), vec);
        assertVecEquals(vec, 2, -13);
        assertSame(vec.set(new Vec2d(-13, 2)), vec);
        assertVecEquals(vec, -13, 2);
    }

    @Test
    public void test_add_components_self() {
        Vec2d vec = new Vec2d(1, 3);
        assertSame(vec.add(41, 2), vec);
        assertVecEquals(vec, 42, 5);
        assertSame(vec.add(-56, -5), vec);
        assertVecEquals(vec, -14, 0);
    }

    @Test
    public void test_add_components_dest() {
        Vec2d vec = new Vec2d(1, 3);
        Vec2d dest = new Vec2d();
        assertSame(vec.add(41, 2, dest), dest);
        assertVecEquals(dest, 42, 5);
        assertSame(vec.add(-56, -5, dest), dest);
        assertVecEquals(dest, -55, -2);
    }

    @Test
    public void test_add_vec_self() {
        Vec2d vec = new Vec2d(1, 3);
        assertSame(vec.add(new Vec2d(41, 2)), vec);
        assertVecEquals(vec, 42, 5);
        assertSame(vec.add(new Vec2d(-56, -5)), vec);
        assertVecEquals(vec, -14, 0);
    }

    @Test
    public void test_add_vec_dest() {
        Vec2d vec = new Vec2d(1, 3);
        Vec2d dest = new Vec2d();
        assertSame(vec.add(new Vec2d(41, 2), dest), dest);
        assertVecEquals(dest, 42, 5);
        assertSame(vec.add(new Vec2d(-56, -5), dest), dest);
        assertVecEquals(dest, -55, -2);
    }

    @Test
    public void test_sub_components_self() {
        Vec2d vec = new Vec2d(4, 2);
        assertSame(vec.sub(5, 11), vec);
        assertVecEquals(vec, -1, -9);
        assertSame(vec.sub(-71, 39), vec);
        assertVecEquals(vec, 70, -48);
    }

    @Test
    public void test_sub_components_dest() {
        Vec2d vec = new Vec2d(4, 2);
        Vec2d dest = new Vec2d();
        assertSame(vec.sub(5, 11, dest), dest);
        assertVecEquals(dest, -1, -9);
        assertSame(vec.sub(-71, 39, dest), dest);
        assertVecEquals(dest, 75, -37);
    }

    @Test
    public void test_sub_vec_self() {
        Vec2d vec = new Vec2d(4, 2);
        assertSame(vec.sub(new Vec2d(5, 11)), vec);
        assertVecEquals(vec, -1, -9);
        assertSame(vec.sub(new Vec2d(-71, 39)), vec);
        assertVecEquals(vec, 70, -48);
    }

    @Test
    public void test_sub_vec_dest() {
        Vec2d vec = new Vec2d(4, 2);
        Vec2d dest = new Vec2d();
        assertSame(vec.sub(new Vec2d(5, 11), dest), dest);
        assertVecEquals(dest, -1, -9);
        assertSame(vec.sub(new Vec2d(-71, 39), dest), dest);
        assertVecEquals(dest, 75, -37);
    }

    @Test
    public void test_mul_scalar_self() {
        Vec2d vec = new Vec2d(74, 2);
        assertSame(vec.mul(0.5), vec);
        assertVecEquals(vec, 37, 1);
        assertSame(vec.mul(-10), vec);
        assertVecEquals(vec, -370, -10);
    }

    @Test
    public void test_mul_scalar_dest() {
        Vec2d vec = new Vec2d(74, 2);
        Vec2d dest = new Vec2d();
        assertSame(vec.mul(0.5, dest), dest);
        assertVecEquals(dest, 37, 1);
        assertSame(vec.mul(-10, dest), dest);
        assertVecEquals(dest, -740, -20);
    }

    @Test
    public void test_mul_components_self() {
        Vec2d vec = new Vec2d(7, -3);
        assertSame(vec.mul(-5, 8), vec);
        assertVecEquals(vec, -35, -24);
        assertSame(vec.mul(10, 10), vec);
        assertVecEquals(vec, -350, -240);
    }

    @Test
    public void test_mul_components_dest() {
        Vec2d vec = new Vec2d(7, -3);
        Vec2d dest = new Vec2d();
        assertSame(vec.mul(-5, 8, dest), dest);
        assertVecEquals(dest, -35, -24);
        assertSame(vec.mul(10, 10, dest), dest);
        assertVecEquals(dest, 70, -30);
    }

    @Test
    public void test_mul_vec_self() {
        Vec2d vec = new Vec2d(7, -3);
        assertSame(vec.mul(new Vec2d(-5, 8)), vec);
        assertVecEquals(vec, -35, -24);
        assertSame(vec.mul(new Vec2d(10, 10)), vec);
        assertVecEquals(vec, -350, -240);
    }

    @Test
    public void test_mul_vec_dest() {
        Vec2d vec = new Vec2d(7, -3);
        Vec2d dest = new Vec2d();
        assertSame(vec.mul(new Vec2d(-5, 8), dest), dest);
        assertVecEquals(dest, -35, -24);
        assertSame(vec.mul(new Vec2d(10, 10), dest), dest);
        assertVecEquals(dest, 70, -30);
    }

    @Test
    public void test_div_scalar_self() {
        Vec2d vec = new Vec2d(84, 40);
        assertSame(vec.div(4), vec);
        assertVecEquals(vec, 21, 10);
        assertSame(vec.div(-0.5), vec);
        assertVecEquals(vec, -42, -20);
    }

    @Test
    public void test_div_scalar_dest() {
        Vec2d vec = new Vec2d(84, 40);
        Vec2d dest = new Vec2d();
        assertSame(vec.div(4, dest), dest);
        assertVecEquals(dest, 21, 10);
        assertSame(vec.div(-0.5, dest), dest);
        assertVecEquals(dest, -168, -80);
    }

    @Test
    public void test_div_components_self() {
        Vec2d vec = new Vec2d(18, 24);
        assertSame(vec.div(9, 8), vec);
        assertVecEquals(vec, 2, 3);
        assertSame(vec.div(2, -6), vec);
        assertVecEquals(vec, 1, -0.5);
    }

    @Test
    public void test_div_components_dest() {
        Vec2d vec = new Vec2d(18, 24);
        Vec2d dest = new Vec2d();
        assertSame(vec.div(9, 8, dest), dest);
        assertVecEquals(dest, 2, 3);
        assertSame(vec.div(2, -6, dest), dest);
        assertVecEquals(dest, 9, -4);
    }

    @Test
    public void test_div_vec_self() {
        Vec2d vec = new Vec2d(18, 24);
        assertSame(vec.div(new Vec2d(9, 8)), vec);
        assertVecEquals(vec, 2, 3);
        assertSame(vec.div(new Vec2d(2, -6)), vec);
        assertVecEquals(vec, 1, -0.5);
    }

    @Test
    public void test_div_vec_dest() {
        Vec2d vec = new Vec2d(18, 24);
        Vec2d dest = new Vec2d();
        assertSame(vec.div(new Vec2d(9, 8), dest), dest);
        assertVecEquals(dest, 2, 3);
        assertSame(vec.div(new Vec2d(2, -6), dest), dest);
        assertVecEquals(dest, 9, -4);
    }

    @Test
    public void test_magnitudeSq() {
        assertEquals(new Vec2d(3, 4).magnitudeSq(), 25, 0.0001);
        assertEquals(new Vec2d(1, -3).magnitudeSq(), 10, 0.0001);
        assertEquals(new Vec2d(-7, -10).magnitudeSq(), 149, 0.0001);
        assertEquals(new Vec2d(-4, -6).magnitudeSq(), 52, 0.0001);
    }

    @Test
    public void test_magnitude() {
        assertEquals(new Vec2d(3, 4).magnitude(), 5, 0.0001);
        assertEquals(new Vec2d(-5, 12).magnitude(), 13, 0.0001);
        assertEquals(new Vec2d(19, -180).magnitude(), 181, 0.0001);
        assertEquals(new Vec2d(-36, -323).magnitude(), 325, 0.0001);
    }

    @Test
    public void test_dot() {
        assertEquals(new Vec2d(1, 1).dot(new Vec2d(4, 2)), 6, 0.0001);
        assertEquals(new Vec2d(-3, 7).dot(new Vec2d(1, 9)), 60, 0.0001);
        assertEquals(new Vec2d(8, 4).dot(new Vec2d(-0.125, -0.25)), -2, 0.0001);
    }

    @Test
    public void test_angle() {
        assertEquals(new Vec2d(1, 2).angle().ccw().rad(), 1.1071, 0.0001);
        assertEquals(new Vec2d(73, -2).angle().ccw().rad(), -0.0273, 0.0001);
        assertEquals(new Vec2d(-8, 92).angle().ccw().rad(), 1.6575, 0.0001);
    }

    @Test
    public void test_angleTo() {
        assertEquals(new Vec2d(1, 0).angleTo(new Vec2d(0, 1)).ccw().deg(), 90, 0.0001);
        assertEquals(new Vec2d(0, 1).angleTo(new Vec2d(1, 0)).ccw().deg(), 90, 0.0001);
        assertEquals(new Vec2d(73, 1).angleTo(new Vec2d(73, 1)).ccw().deg(), 0, 0.0001);
    }

    @Test
    public void test_distanceToSq_components() {
        assertEquals(new Vec2d(4, 6).distanceToSq(1, 2), 25, 0.0001);
        assertEquals(new Vec2d(-1, -1).distanceToSq(-2, 2), 10, 0.0001);
        assertEquals(new Vec2d(-7, -10).distanceToSq(0, 0), 149, 0.0001);
        assertEquals(new Vec2d(-14, -16).distanceToSq(-10, -10), 52, 0.0001);
    }

    @Test
    public void test_distanceToSq_vec() {
        assertEquals(new Vec2d(4, 6).distanceToSq(new Vec2d(1, 2)), 25, 0.0001);
        assertEquals(new Vec2d(-1, -1).distanceToSq(new Vec2d(-2, 2)), 10, 0.0001);
        assertEquals(new Vec2d(-7, -10).distanceToSq(new Vec2d(0, 0)), 149, 0.0001);
        assertEquals(new Vec2d(-14, -16).distanceToSq(new Vec2d(-10, -10)), 52, 0.0001);
    }

    @Test
    public void test_distanceTo_components() {
        assertEquals(new Vec2d(13, 14).distanceTo(10, 10), 5, 0.0001);
        assertEquals(new Vec2d(-7, 14).distanceTo(-2, 2), 13, 0.0001);
        assertEquals(new Vec2d(15, -170).distanceTo(-4, 10), 181, 0.0001);
        assertEquals(new Vec2d(-36, -323).distanceTo(0, 0), 325, 0.0001);
    }

    @Test
    public void test_distanceTo_vec() {
        assertEquals(new Vec2d(13, 14).distanceTo(new Vec2d(10, 10)), 5, 0.0001);
        assertEquals(new Vec2d(-7, 14).distanceTo(new Vec2d(-2, 2)), 13, 0.0001);
        assertEquals(new Vec2d(15, -170).distanceTo(new Vec2d(-4, 10)), 181, 0.0001);
        assertEquals(new Vec2d(-36, -323).distanceTo(new Vec2d(0, 0)), 325, 0.0001);
    }

    @Test
    public void test_rotateBy_self() {
        assertVecEquals(new Vec2d(0, 1).rotateBy(CCWAngle.deg(90)), -1, 0);
        assertVecEquals(new Vec2d(-1, 0).rotateBy(CWAngle.deg(270)), 0, -1);
    }

    @Test
    public void test_rotateBy_dest() {
        Vec2d vec = new Vec2d(0, 1);
        Vec2d dest = new Vec2d();
        assertSame(vec.rotateBy(CCWAngle.deg(90), dest), dest);
        assertVecEquals(dest, -1, 0);
        assertSame(vec.rotateBy(CWAngle.deg(180), dest), dest);
        assertVecEquals(dest, 0, -1);
    }

    @Test
    public void test_normalize_self() {
        Vec2d vec = new Vec2d(1, 2);
        assertSame(vec.normalize(), vec);
        assertEquals(vec.magnitude(), 1, 0.0001);
        assertEquals(vec.angleTo(new Vec2d(1, 2)).ccw().rad(), 0, 0.0001);

        vec = new Vec2d(-3, 6);
        assertSame(vec.normalize(), vec);
        assertEquals(vec.magnitude(), 1, 0.0001);
        assertEquals(vec.angleTo(new Vec2d(-3, 6)).ccw().rad(), 0, 0.0001);
    }

    @Test
    public void test_normalize_dest() {
        Vec2d dest = new Vec2d();

        Vec2d vec = new Vec2d(1, 2);
        assertSame(vec.normalize(dest), dest);
        assertVecEquals(vec, 1, 2);
        assertEquals(dest.magnitude(), 1, 0.0001);
        assertEquals(dest.angleTo(vec).ccw().rad(), 0, 0.0001);

        vec = new Vec2d(-3, 6);
        assertSame(vec.normalize(dest), dest);
        assertVecEquals(vec, -3, 6);
        assertEquals(dest.magnitude(), 1, 0.0001);
        assertEquals(dest.angleTo(dest).ccw().rad(), 0, 0.0001);
    }

    @Test
    public void test_boxNormalize_self() {
        Vec2d vec = new Vec2d(39, 39);
        assertSame(vec.boxNormalize(), vec);
        assertVecEquals(vec, 1, 1);
        assertEquals(vec.angleTo(new Vec2d(39, 39)).ccw().rad(), 0, 0.0001);

        vec = new Vec2d(-2, 1);
        assertSame(vec.boxNormalize(), vec);
        assertVecEquals(vec, -1, 0.5);
        assertEquals(vec.angleTo(new Vec2d(-2, 1)).ccw().rad(), 0, 0.0001);
    }

    @Test
    public void test_boxNormalize_dest() {
        Vec2d dest = new Vec2d();

        Vec2d vec = new Vec2d(39, 39);
        assertSame(vec.boxNormalize(dest), dest);
        assertVecEquals(vec, 39, 39);
        assertVecEquals(dest, 1, 1);
        assertEquals(dest.angleTo(new Vec2d(39, 39)).ccw().rad(), 0, 0.0001);

        vec = new Vec2d(-2, 1);
        assertSame(vec.boxNormalize(dest), dest);
        assertVecEquals(vec, -2, 1);
        assertVecEquals(dest, -1, 0.5);
        assertEquals(dest.angleTo(new Vec2d(-2, 1)).ccw().rad(), 0, 0.0001);
    }

    @Test
    public void test_negate_self() {
        Vec2d vec = new Vec2d(5, -1);
        assertSame(vec.negate(), vec);
        assertVecEquals(vec, -5, 1);

        vec = new Vec2d(498, 92);
        assertSame(vec.negate(), vec);
        assertVecEquals(vec, -498, -92);
    }

    @Test
    public void test_negate_dest() {
        Vec2d dest = new Vec2d();

        Vec2d vec = new Vec2d(5, -1);
        assertSame(vec.negate(dest), dest);
        assertVecEquals(vec, 5, -1);
        assertVecEquals(dest, -5, 1);

        vec = new Vec2d(498, 92);
        assertSame(vec.negate(dest), dest);
        assertVecEquals(vec, 498, 92);
        assertVecEquals(dest, -498, -92);
    }

    @Test
    public void test_absolute_self() {
        Vec2d vec = new Vec2d(5, -1);
        assertSame(vec.absolute(), vec);
        assertVecEquals(vec, 5, 1);

        vec = new Vec2d(-498, -92);
        assertSame(vec.absolute(), vec);
        assertVecEquals(vec, 498, 92);
    }

    @Test
    public void test_absolute_dest() {
        Vec2d dest = new Vec2d();

        Vec2d vec = new Vec2d(5, -1);
        assertSame(vec.absolute(dest), dest);
        assertVecEquals(vec, 5, -1);
        assertVecEquals(dest, 5, 1);

        vec = new Vec2d(-498, -92);
        assertSame(vec.absolute(dest), dest);
        assertVecEquals(vec, -498, -92);
        assertVecEquals(dest, 498, 92);
    }

    @Test
    public void test_equals() {
        Vec2d vec = new Vec2d(1, 2);
        assertEquals(vec, vec);

        assertEquals(new Vec2d(1, 2), new Vec2d(1, 2));
        assertNotEquals(new Vec2d(934, 2), new Vec2d(1, 2));
        assertNotEquals(new Vec2d(1, -2), new Vec2d(1, 2));
        assertNotEquals(
                new Vec2d(834.1238, 734971192.1723), new Vec2d(-1823471.374, 89713962897429.3));
    }

    @Test
    public void test_hashCode() {
        assertEquals(new Vec2d(1, 2).hashCode(), new Vec2d(1, 2).hashCode());
        assertEquals(new Vec2d(0, 12).hashCode(), new Vec2d(0, 12).hashCode());
    }

    @Test
    public void test_toString() {
        assertEquals(new Vec2d(1, 2).toString(), "(1.000, 2.000)");
        assertEquals(new Vec2d(-12.34, 912.412).toString(), "(-12.340, 912.412)");
    }
}
