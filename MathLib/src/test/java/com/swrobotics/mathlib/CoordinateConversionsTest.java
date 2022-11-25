package com.swrobotics.mathlib;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import org.junit.Test;

import static com.swrobotics.mathlib.CoordinateConversions.*;
import static com.swrobotics.mathlib.MathTestUtils.assertFuzzyEquals;
import static org.junit.Assert.assertEquals;

public final class CoordinateConversionsTest {
    @Test
    public void test_fromWPICoords() {
        assertFuzzyEquals(fromWPICoords(new Translation2d(0, 0)), new Vec2d(0, 0), 0.0001);
        assertFuzzyEquals(fromWPICoords(new Translation2d(1, 0)), new Vec2d(0, 1), 0.0001);
        assertFuzzyEquals(fromWPICoords(new Translation2d(0, 1)), new Vec2d(-1, 0), 0.0001);
        assertFuzzyEquals(fromWPICoords(new Translation2d(-1, 0)), new Vec2d(0, -1), 0.0001);
        assertFuzzyEquals(fromWPICoords(new Translation2d(0, -1)), new Vec2d(1, 0), 0.0001);
        assertFuzzyEquals(fromWPICoords(new Translation2d(81, 91)), new Vec2d(-91, 81), 0.0001);
    }

    @Test
    public void test_toWPICoords() {
        assertEquals(toWPICoords(new Vec2d(0, 0)), new Translation2d(0, 0));
        assertEquals(toWPICoords(new Vec2d(0, 1)), new Translation2d(1, 0));
        assertEquals(toWPICoords(new Vec2d(-1, 0)), new Translation2d(0, 1));
        assertEquals(toWPICoords(new Vec2d(0, -1)), new Translation2d(-1, 0));
        assertEquals(toWPICoords(new Vec2d(1, 0)), new Translation2d(0, -1));
        assertEquals(toWPICoords(new Vec2d(-91, 81)), new Translation2d(81, 91));
    }

    @Test
    public void test_fromWPIAngle() {
        assertEquals(fromWPIAngle(Rotation2d.fromDegrees(0)).ccw().deg(), 90, 0.0001);
        assertEquals(fromWPIAngle(Rotation2d.fromDegrees(1)).ccw().deg(), 91, 0.0001);
        assertEquals(fromWPIAngle(Rotation2d.fromDegrees(-1)).ccw().deg(), 89, 0.0001);
    }

    @Test
    public void test_toWPIAngle() {
        assertEquals(toWPIAngle(CCWAngle.deg(90)), Rotation2d.fromDegrees(0));
        assertEquals(toWPIAngle(CCWAngle.deg(91)), Rotation2d.fromDegrees(1));
        assertEquals(toWPIAngle(CCWAngle.deg(89)), Rotation2d.fromDegrees(-1));
    }
}
