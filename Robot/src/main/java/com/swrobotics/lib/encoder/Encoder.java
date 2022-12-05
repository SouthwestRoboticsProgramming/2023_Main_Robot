package com.swrobotics.lib.encoder;

import java.util.function.Supplier;

import com.swrobotics.mathlib.Angle;

/**
 * A class that allows all types of encoders to work in the same manner.
 */
public abstract class Encoder implements Supplier<Angle> {

    private Angle positionOffset = Angle.ZERO; // Applied by subtracting
    private Angle velocityOffset = Angle.ZERO; // Applied by subtracting
    
    private boolean inverted = false;
    
    protected abstract Angle getRawAngleImpl();
    protected abstract Angle getVelocityImpl();

    public Angle getRawAngle() {
        if (inverted) return getRawAngleImpl().negate();
        return getRawAngleImpl();
    }

    public Angle getRawVelocity() {
        if (inverted) return getVelocityImpl().negate();
        return getVelocityImpl();
    
    }

    public Angle getVelocity() {
        return getRawVelocity().ccw().sub(velocityOffset.ccw());
    }

    /**
     * Sets the current angle of the encoder, all angles reported by {@code getAngle()}
     * will be relative to this position.
     * @param newAngle
     */
    public void setAngle(Angle newAngle) {
        positionOffset = getRawAngle().ccw().sub(newAngle.ccw());
    }

    public Angle getAngle() {
        return getRawAngle().ccw().sub(positionOffset.ccw());
    }

    public void setVelocity(Angle newVelocity) {
        velocityOffset = getRawVelocity().ccw().sub(newVelocity.ccw());
    }

    public void setVelocityOffset(Angle offset) {
        velocityOffset = offset;
    }

    public boolean getInverted() {
        return inverted;
    }

    /**
     * Set if the motor should be inverted
     * @param inverted If the motor should be inverted
     */
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public Angle get() {
        return getAngle();
    }

}
