package com.swrobotics.lib.swerve;

import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.math.kinematics.SwerveModuleState;

public class SwerveModule {
    
    public SwerveModule() {

    }

    public Vec2d getPosition() {
        return new Vec2d();
    }

    public void setState(SwerveModuleState state) {
        
    }

    public SwerveModuleState getState() {
        return new SwerveModuleState();
    }
}
