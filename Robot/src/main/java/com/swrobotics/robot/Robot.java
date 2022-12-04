package com.swrobotics.robot;

import com.swrobotics.lib.encoder.CANCoderEncoder;
import com.swrobotics.lib.gyro.PigeonGyroscope;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.lib.motor.calc.PIDCalculator;
import com.swrobotics.lib.motor.calc.VelocityCalculator;
import com.swrobotics.lib.motor.ctre.TalonSRXMotor;
import com.swrobotics.lib.schedule.Scheduler;
import com.swrobotics.lib.swerve.SwerveDrive;
import com.swrobotics.lib.swerve.SwerveDrive2;
import com.swrobotics.lib.swerve.SwerveModule;
import com.swrobotics.lib.wpilib.AbstractRobot;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.control.Input;
import com.swrobotics.robot.control.XboxInput;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.PowerDistribution;

public final class Robot extends AbstractRobot {
    private static final double PERIODIC_PER_SECOND = 50;

    // private final SwerveDrive drive = new SwerveDrive(
    //     new PigeonGyroscope(0),
    // //     getSwerveModule(new Vec2d(-3, 3)),
    // //     getSwerveModule(new Vec2d(3, 3)),
    // //     getSwerveModule(new Vec2d(-3, -3)),
    // //     getSwerveModule(new Vec2d(3, -3))
    // // );

    private final SwerveDrive2 drive = new SwerveDrive2();

    public Robot() {
	    super(PERIODIC_PER_SECOND);
    }
    
    @Override
    protected final void addSubsystems() {
        Scheduler sch = Scheduler.get();

        sch.addSubsystem(drive);
        // drive.setChassisSpeeds(new ChassisSpeeds(1, 1, Math.PI / 4));
    }


    // Temporary
    // private SwerveModule getSwerveModule(Vec2d position) {

    //     PIDController pid = new PIDController(2.3, 0, 0);
    //     SimpleMotorFeedforward feedforward = new SimpleMotorFeedforward(0.587, 2.3, 0.0917);

    //     double driveGearRatio = 8.14; // From Swerve Drive Specialties
    //     double turnGearRatio = 150.0 / 7.0; // From openalliance build thread
    //     double wheelRadiusMeters = Units.inchesToMeters(2.0); // From measurement

    //     Motor driveMotor = new TalonSRXMotor(drive, 0);
    //     Motor turnMotor = new TalonSRXMotor(drive, 1);
    //     CANCoderEncoder encoder = new CANCoderEncoder(2);

    //     driveMotor.setVelocityCalculator(
    //         new VelocityCalculator() {

    //             @Override
    //             public void reset() {
    //                 pid.reset();
    //             }

    //             @Override
    //             public double calculate(Angle currentVelocity, Angle targetVelocity) {
    //                 double pidOutput = pid.calculate(currentVelocity.abs().rot(), targetVelocity.abs().rot());
    //                 double feedOutput = feedforward.calculate(targetVelocity.abs().rot());
    //                 return pidOutput + feedOutput;
    //             }
                
    //         }
    //     );

    //     SwerveModule module = new SwerveModule(
    //         driveMotor,
    //         turnMotor,
    //         encoder,
    //         position,
    //         driveGearRatio,
    //         turnGearRatio,
    //         wheelRadiusMeters);

    //     module.configureSimulation(DCMotor.getFalcon500(1), 0.0025, DCMotor.getFalcon500(1), 0.004096955);

    //     return module;

    // }
}
