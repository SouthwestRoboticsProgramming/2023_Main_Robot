package com.swrobotics.lib.motor.rev;

import com.revrobotics.SparkMaxPIDController;
import com.swrobotics.mathlib.Angle;
import edu.wpi.first.math.util.Units;

public final class SmartMotionSlot {
    private final SparkMaxPIDController pid;
    private final int index;

    SmartMotionSlot(SparkMaxPIDController pid, int index) {
        this.pid = pid;
        this.index = index;
    }

    public void setAccelStrategy(SparkMaxPIDController.AccelStrategy strategy) {
        pid.setSmartMotionAccelStrategy(strategy, index);
    }

    /**
     * @param allowedErr max rotational deviation from setpoint
     */
    public void setAllowedClosedLoopError(Angle allowedErr) {
        pid.setSmartMotionAllowedClosedLoopError(allowedErr.ccw().abs().rot(), index);
    }

    /**
     * @param maxAccel max acceleration in angle per second per second
     */
    public void setMaxAccel(Angle maxAccel) {
        pid.setSmartMotionMaxAccel(Units.radiansPerSecondToRotationsPerMinute(maxAccel.ccw().abs().rad()), index);
    }

    /**
     * @param maxVel max velocity in angle per second
     */
    public void setMaxVelocity(Angle maxVel) {
        pid.setSmartMotionMaxVelocity(Units.radiansPerSecondToRotationsPerMinute(maxVel.ccw().abs().rad()), index);
    }

    /**
     * @param minOutVel minimum output velocity in angle per second
     */
    public void setMinOutputVelocity(Angle minOutVel) {
        pid.setSmartMotionMinOutputVelocity(Units.radiansPerSecondToRotationsPerMinute(minOutVel.ccw().abs().rad()), index);
    }
}
