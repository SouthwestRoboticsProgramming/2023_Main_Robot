package com.swrobotics.lib.swerve;

import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.lib.schedule.Subsystem;
import com.swrobotics.lib.wpilib.AbstractRobot;
import com.swrobotics.mathlib.AbsoluteAngle;
import com.swrobotics.mathlib.AbstractAngle;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CoordinateConversions;
import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

public class SwerveModule implements Subsystem {

    private final Motor drive;
    private final double driveGearRatio;
    private final double metersToRadians;
    private FlywheelSim driveSim;

    private final Motor turn;
    private final double turnGearRatio;
    private FlywheelSim turnSim;

    private final Encoder driveEncoder;
    private final Encoder turnEncoder;
    private final Translation2d position;

    // Temporary
    private SwerveModuleState targetState;
    
    /**
     * A single swerve module capable of both steering and driving
     * 
     * @param drive
     * @param turn
     * @param encoder
     * @param position
     * @param driveGearRatio The gear ratio of DriveMotor:Output. 1:8 becomes 1/8
     * @param turnGearRatio
     */
    public SwerveModule(Motor drive, Motor turn, Encoder encoder, Vec2d position, double driveGearRatio, double turnGearRatio, double wheelRadiusMeters) {
        this.drive = drive;
        this.driveGearRatio = driveGearRatio;
        metersToRadians = 1.0 / (driveGearRatio * wheelRadiusMeters);

        this.turn = turn;
        this.turnGearRatio = turnGearRatio;

        targetState = new SwerveModuleState(); // Default state is forward, stopped

        turnEncoder = encoder;
        driveEncoder = new Encoder() {

            @Override
            protected Angle getRawAngleImpl() {
                return Angle.ZERO;
            }

            @Override
            protected Angle getVelocityImpl() {
                if (driveSim != null) {
                    return AbsoluteAngle.rad(driveSim.getAngularVelocityRadPerSec());
                }

                return Angle.ZERO;
            }
            
        };

        drive.setEncoder(driveEncoder);

        // Convert from local coordinates to global coordinates
        this.position = CoordinateConversions.toWPICoords(position);
    }

    public Translation2d getPosition() {
        return position;
    }

    public void setState(SwerveModuleState state) {
        targetState = state;

        AbsoluteAngle targetVelocity = AbsoluteAngle.rad(
            state.speedMetersPerSecond * metersToRadians
        );

        // Set the motors to outputs
        drive.velocity(targetVelocity);
        // TODO: Rotation
    }

    public SwerveModuleState getState() {

        // Simulate state using FlywheelSim
        if (AbstractRobot.isSimulation() && driveSim != null) {
            double driveVelocity = driveSim.getAngularVelocityRadPerSec() / metersToRadians;

            // Temporary
            return new SwerveModuleState(driveVelocity, targetState.angle);
        }

        return targetState; // Output is exactly the same as input
    }

    public void configureSimulation(DCMotor driveMotorType, double driveMOI, DCMotor turnMotorType, double turnMOI) {
        driveSim = new FlywheelSim(driveMotorType, 1 / driveGearRatio, driveMOI);
        turnSim = new FlywheelSim(turnMotorType, 1 / turnGearRatio, turnMOI);
    }

    @Override
    public void periodic() {

        // Take battery draw into account
        double batteryVoltage = RobotController.getBatteryVoltage();

        // Calculate voltages given current calculated outputs
        double driveVoltage = drive.getCurrentPercentOutput() * batteryVoltage;
        double turnVoltage = turn.getCurrentPercentOutput() * batteryVoltage;

        // Simulate the system under those voltages
        driveSim.setInputVoltage(driveVoltage);
        turnSim.setInputVoltage(turnVoltage);

        System.out.println("Drive Voltage: " + driveVoltage);

        // Update the motor timestep for accurate calculations.
        driveSim.update(0.02);
        turnSim.update(0.02);
    }
}
