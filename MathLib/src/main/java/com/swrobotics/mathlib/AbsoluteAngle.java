package com.swrobotics.mathlib;

/**
 * Represents either a directionless angle, or an angle without a known
 * direction. Note: It is still possible for an absolute angle to be negative.
 * Being an instance of this class only indicates that it has no direction.
 */
public final class AbsoluteAngle extends AbstractAngle<AbsoluteAngle> {
    /**
     * Creates a new absolute angle from a radian measurement.
     *
     * @param rad radian measurement
     * @return new instance
     */
    public static AbsoluteAngle rad(double rad) {
        return new AbsoluteAngle(rad);
    }

    /**
     * Creates a new absolute angle from a degree measurement.
     *
     * @param deg degree measurement
     * @return new instance
     */
    public static AbsoluteAngle deg(double deg) {
        return new AbsoluteAngle(Math.toRadians(deg));
    }

    /**
     * Creates a new absolute angle from a rotation measurement.
     *
     * @param rot rotation measurement
     * @return new instance
     */
    public static AbsoluteAngle rot(double rot) {
        return new AbsoluteAngle(rot * MathUtil.TAU);
    }

    // Store results from ccw() and cw() to reduce object creation
    CCWAngle cacheCCW;
    CWAngle cacheCW;

    private AbsoluteAngle(double rad) {
        super(rad);
    }

    @Override
    protected AbsoluteAngle create(double rad) {
        return new AbsoluteAngle(rad);
    }

    @Override
    public AbsoluteAngle abs() {
        return this;
    }

    @Override
    public CCWAngle ccw() {
        if (cacheCCW == null) {
            cacheCCW = CCWAngle.rad(rad());
            cacheCCW.cacheAbs = this;
        }
        return cacheCCW;
    }

    @Override
    public CWAngle cw() {
        if (cacheCW == null) {
            cacheCW = CWAngle.rad(rad());
            cacheCW.cacheAbs = this;
        }
        return cacheCW;
    }

    @Override
    public String toString() {
        return String.format("%.3f deg abs", deg());
    }
}
