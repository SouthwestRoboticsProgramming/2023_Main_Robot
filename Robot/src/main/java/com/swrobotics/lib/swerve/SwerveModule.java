package com.swrobotics.lib.swerve;

import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.lib.wpilib.AbstractRobot;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CoordinateConversions;
import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

public class SwerveModule {

    private final Motor drive;
    private final double driveGearRatio;
    private FlywheelSim driveSim;

    private final Motor turn;
    private final double turnGearRatio;
    private FlywheelSim turnSim;

    private final Encoder driveEncoder;
    private final Encoder turnEncoder;
    private final Translation2d position;

    // Temporary
    private SwerveModuleState state;
    
    public SwerveModule(Motor drive, Motor turn, Encoder encoder, Vec2d position, double driveGearRatio, double turnGearRatio) {
        this.drive = drive;
        this.driveGearRatio = driveGearRatio;

        this.turn = turn;
        this.turnGearRatio = turnGearRatio;

        state = new SwerveModuleState(); // Default state is forward, stopped

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
        this.state = state;
    }

    public SwerveModuleState getState() {
        // Velocity
        if (AbstractRobot.get().isDisabled()) { return new SwerveModuleState(); }

        if (state == null) { return new SwerveModuleState(); }

        // Simulate state using FlywheelSim
        if (AbstractRobot.isSimulation()) {
            
        }

        return state; // Output is exactly the same as input
    }

    public void configureSimulation(DCMotor driveMotorType, double driveMOI, DCMotor turnMotorType, double turnMOI) {
        driveSim = new FlywheelSim(driveMotorType, driveGearRatio, driveMOI);
        turnSim = new FlywheelSim(turnMotorType, turnGearRatio, turnMOI);
    }
}
