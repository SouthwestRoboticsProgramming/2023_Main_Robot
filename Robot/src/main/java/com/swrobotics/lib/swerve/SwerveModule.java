package com.swrobotics.lib.swerve;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;

public class SwerveModule {
    
    public SwerveModule() {

    }

    public Translation2d getPosition() {
        return new Translation2d();
    }

    public void setState(SwerveModuleState state) {
        
    }

    public SwerveModuleState getState() {
        return new SwerveModuleState();
    }
}
