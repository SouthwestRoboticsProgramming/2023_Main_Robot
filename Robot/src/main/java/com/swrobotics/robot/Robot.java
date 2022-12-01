package com.swrobotics.robot;

import com.swrobotics.lib.encoder.CANCoderEncoder;
import com.swrobotics.lib.gyro.PigeonGyroscope;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.lib.motor.calc.PIDCalculator;
import com.swrobotics.lib.motor.ctre.TalonSRXMotor;
import com.swrobotics.lib.schedule.Scheduler;
import com.swrobotics.lib.swerve.SwerveDrive;
import com.swrobotics.lib.swerve.SwerveModule;
import com.swrobotics.lib.wpilib.AbstractRobot;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.control.Input;
import com.swrobotics.robot.control.XboxInput;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.system.plant.DCMotor;

public final class Robot extends AbstractRobot {
    private static final double PERIODIC_PER_SECOND = 50;

    private final SwerveDrive drive = new SwerveDrive(
        new PigeonGyroscope(0),
        getSwerveModule(new Vec2d(-3, 3)),
        getSwerveModule(new Vec2d(3, 3)),
        getSwerveModule(new Vec2d(-3, -3)),
        getSwerveModule(new Vec2d(3, -3))
    );

    public Robot() {
	    super(PERIODIC_PER_SECOND);
    }
    
    @Override
    protected final void addSubsystems() {
        Scheduler sch = Scheduler.get();

        sch.addSubsystem(drive);
        drive.setChassisSpeeds(new ChassisSpeeds(1, 1, Math.PI / 4));
    }


    // Temporary
    private SwerveModule getSwerveModule(Vec2d position) {

        double driveGearRatio = 1 / 8.14; // From Swerve Drive Specialties
        double turnGearRatio = 150.0 / 7.0; // From openalliance build thread
        double wheelRadiusMeters = 0.05; // From measurement

        Motor driveMotor = new TalonSRXMotor(drive, 0);
        Motor turnMotor = new TalonSRXMotor(drive, 1);
        CANCoderEncoder encoder = new CANCoderEncoder(2);

        driveMotor.setVelocityCalculator(
            new PIDCalculator(0.0001, 0, 0)
        );

        SwerveModule module = new SwerveModule(
            driveMotor,
            turnMotor,
            encoder,
            position,
            driveGearRatio,
            turnGearRatio,
            wheelRadiusMeters);

        module.configureSimulation(DCMotor.getFalcon500(1), 0.025, DCMotor.getFalcon500(1), 0.004096955);

        return module;

    }
}
