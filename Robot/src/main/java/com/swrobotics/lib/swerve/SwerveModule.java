package com.swrobotics.lib.swerve;

import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CoordinateConversions;
import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;

public class SwerveModule {

    private final Motor drive;
    private final Motor turn;
    private final Encoder driveEncoder;
    private final Encoder turnEncoder;
    private final Translation2d position;
    
    public SwerveModule(Motor drive, Motor turn, Encoder encoder, Vec2d position) {
        this.drive = drive;
        this.turn = turn;
        turnEncoder = encoder;
        // driveEncoder = drive.getEncoder();
        driveEncoder = new Encoder() {

            @Override
            protected Angle getRawAngleImpl() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            protected Angle getVelocityImpl() {
                // TODO Auto-generated method stub
                return null;
            }
            
        };

        // Convert from local coordinates to global coordinates
        this.position = CoordinateConversions.toWPICoords(position);
    }

    public Translation2d getPosition() {
        return position;
    }

    public void setState(SwerveModuleState state) {
        
    }

    public SwerveModuleState getState() {
        // Velocity


        return new SwerveModuleState(1.5, new Rotation2d(45));
    }
}
