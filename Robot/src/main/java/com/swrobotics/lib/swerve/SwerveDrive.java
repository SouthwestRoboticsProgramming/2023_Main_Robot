package com.swrobotics.lib.swerve;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;

public class SwerveDrive {

    private final SwerveModule[] modules;
    private final SwerveDriveKinematics kinematics;

    public SwerveDrive(SwerveModule[] modules) {
        this.modules = modules;

        // Extract positions of modules for kinematics
        Translation2d[] modulePositions = new Translation2d[modules.length];
        for (int i = 0; i < modules.length; i++) {
            modulePositions[i] = modules[i].getPosition();
        }

        // Construct kinematics that describe how the drive should move
        kinematics = new SwerveDriveKinematics(modulePositions);
    }

    public void setChassisSpeeds(ChassisSpeeds speeds) {
        // Convert target body motion into indevidual wheel states
        SwerveModuleState[] states = kinematics.toSwerveModuleStates(speeds); // TODO: Center of rotation

        // Set the modules to target these states
        for (int i = 0; i < modules.length; i++) {
            modules[i].setState(states[i]);
        }
    }

    public ChassisSpeeds getChassisSpeeds() {
        // Read modules states from the modules
        SwerveModuleState[] states = new SwerveModuleState[modules.length];
        for (int i = 0; i < modules.length; i++) {
            states[i] = modules[i].getState();
        }

        // Kinematics calculates how the robot should be moving
        return kinematics.toChassisSpeeds(states);
    }

    public Vec2d getPosition() {
        return new Vec2d();
    }

    public Angle getRotation() {
        return CCWAngle.ZERO;
    }
}
