package com.swrobotics.lib.swerve;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.math.kinematics.ChassisSpeeds;

public class SwerveDrive {
    public SwerveDrive() {

    }

    public void set(ChassisSpeeds speeds) {

    }

    public Vec2d getPosition() {
        return new Vec2d();
    }

    public Angle getRotation() {
        return CCWAngle.ZERO;
    }
}
