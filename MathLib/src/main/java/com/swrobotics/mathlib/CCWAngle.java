package com.swrobotics.mathlib;

/** Represents a counterclockwise angle. */
public final class CCWAngle extends AbstractAngle<CCWAngle> {
    /**
     * Creates a new counterclockwise angle from a radian measurement.
     *
     * @param rad radian measurement
     * @return new instance
     */
    public static CCWAngle rad(double rad) {
        return new CCWAngle(rad);
    }

    /**
     * Creates a new counterclockwise angle from a degree measurement.
     *
     * @param deg degree measurement
     * @return new instance
     */
    public static CCWAngle deg(double deg) {
        return new CCWAngle(Math.toRadians(deg));
    }

    /**
     * Creates a new counterclockwise angle from a rotation measurement.
     *
     * @param rot rotation measurement
     * @return new instance
     */
    public static CCWAngle rot(double rot) {
        return new CCWAngle(rot * MathUtil.TAU);
    }

    // Store result from cw() to reduce object creation
    CWAngle cacheCW;

    private CCWAngle(double rad) {
        super(rad);
    }

    @Override
    protected CCWAngle create(double rad) {
        return new CCWAngle(rad);
    }

    @Override
    public CCWAngle ccw() {
        return this;
    }

    @Override
    public CWAngle cw() {
        if (cacheCW == null) {
            cacheCW = CWAngle.rad(-rad());
        }
        return cacheCW;
    }

    @Override
    public String toString() {
        return String.format("%.3f deg ccw", deg());
    }
}
