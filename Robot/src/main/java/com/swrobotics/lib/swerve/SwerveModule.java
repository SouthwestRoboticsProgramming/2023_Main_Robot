/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.swrobotics.lib.swerve;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.schedule.Subsystem;
import com.swrobotics.lib.wpilib.AbstractRobot;
import com.swrobotics.mathlib.AbsoluteAngle;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

import static com.swrobotics.lib.swerve.Constants.ModuleConstants.*;

public class SwerveModule implements Subsystem {
	int mModuleNumber;

	private SwerveModuleState outputState = new SwerveModuleState();

	public final TalonFX mTurningMotor;
	public final TalonFX mDriveMotor;
	double mZeroOffset;
	boolean mInverted;

	static double kF;
	static double kP;
	static double kI;
	static double kD;
	int kI_Zone = 900;
	int kMaxIAccum = 1000000;
	int kErrorBand = 50;

	int kCruiseVelocity = 14000;
	int kMotionAcceleration = kCruiseVelocity * 10;

	private double m_turnOutput;
	private double m_driveOutput;

	private final PIDController m_drivePIDController = new PIDController(kPModuleDriveController, 0, 0);

	private final ProfiledPIDController m_turningPIDController = new ProfiledPIDController(kPModuleTurningController, 0, 0,
			new TrapezoidProfile.Constraints(kMaxModuleAngularSpeedRadiansPerSecond,
					kMaxModuleAngularAccelerationRadiansPerSecondSquared));

	// Gains are for example purposes only - must be determined for your own robot!
	private final SimpleMotorFeedforward m_driveFeedforward = new SimpleMotorFeedforward(0.587, 2.3, 0.0917);
	private final SimpleMotorFeedforward m_turnFeedforward = new SimpleMotorFeedforward(1, 0.5);

	private double simTurnEncoderDistance;
	private double simThrottleEncoderDistance;

	private Encoder driveEncoder;
	private Encoder turnEncoder;

	private final FlywheelSim moduleRotationSimModel = new FlywheelSim(
			LinearSystemId.identifyVelocitySystem(0.16, kaVoltSecondsSquaredPerRadian),
			DCMotor.getFalcon500(1),
			kTurningMotorGearRatio);

	private final FlywheelSim moduleThrottleSimModel = new FlywheelSim(
			LinearSystemId.identifyVelocitySystem(2, 1.24),
			DCMotor.getFalcon500(1),
			kDriveMotorGearRatio);

	public SwerveModule(int moduleNumber, TalonFX TurningMotor, TalonFX driveMotor, double zeroOffset,
			boolean invertTurn, boolean invertThrottle) {
		mModuleNumber = moduleNumber;
		mTurningMotor = TurningMotor;
		mDriveMotor = driveMotor;
		mZeroOffset = zeroOffset;

		mTurningMotor.configFactoryDefault();
		mTurningMotor.configOpenloopRamp(0.1);
		mTurningMotor.configClosedloopRamp(0.1);

		mTurningMotor.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
		mDriveMotor.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
		mTurningMotor.setInverted(invertTurn);
		mDriveMotor.setInverted(invertThrottle);
		mTurningMotor.setSelectedSensorPosition(0);
		mDriveMotor.setSelectedSensorPosition(0);

		mTurningMotor.config_kF(0, kF);
		mTurningMotor.config_kP(0, kP);
		mTurningMotor.config_kI(0, kI);
		mTurningMotor.config_IntegralZone(0, kI_Zone);
		mTurningMotor.configMaxIntegralAccumulator(0, kMaxIAccum);
		mTurningMotor.config_kD(0, kD);
		mTurningMotor.configMotionCruiseVelocity(kCruiseVelocity);
		mTurningMotor.configMotionAcceleration(kMotionAcceleration);
		mTurningMotor.configAllowableClosedloopError(0, kErrorBand);

		mTurningMotor.setNeutralMode(NeutralMode.Brake);
		mDriveMotor.setNeutralMode(NeutralMode.Brake);

		// Limit the PID Controller's input range between -pi and pi and set the input
		// to be continuous.
		m_turningPIDController.enableContinuousInput(-Math.PI, Math.PI);

		// Setup dummy encoders for simulation
		if (AbstractRobot.isSimulation()) {

			// Simulate drive encoder
			driveEncoder = new com.swrobotics.lib.encoder.Encoder() {
				@Override
				protected Angle getRawAngleImpl() { return Angle.ZERO; }

				@Override
				protected Angle getVelocityImpl() { return Angle.ZERO; }
			};

			// Simulate turn encoder
			turnEncoder = new com.swrobotics.lib.encoder.Encoder() {

				@Override
				protected Angle getRawAngleImpl() { return Angle.ZERO; }

				@Override
				protected Angle getVelocityImpl() { return Angle.ZERO; }
			};
		}
	}

	/**
	 * Resets the module's encoders and controllers
	 */
	public void reset() {
		mTurningMotor.setSelectedSensorPosition(0);
		mDriveMotor.setSelectedSensorPosition(0);

		turnEncoder.setAngle(Angle.ZERO);
		driveEncoder.setAngle(Angle.ZERO);

		// TODO: Reset controllers
	}

	public Rotation2d getHeading() {
		return new Rotation2d(getTurningRadians());
	}

	/**
	 * Returns the current angle of the module.
	 *
	 * @return The current angle of the module in radians.
	 */
	public double getTurningRadians() {
		if (RobotBase.isReal())
			return mTurningMotor.getSelectedSensorPosition()
					* Constants.ModuleConstants.kTurningEncoderDistancePerPulse;
		else
			return turnEncoder.getAngle().abs().rad();
		// return turnEncoder.getDistance();
		// return outputState.angle.getRadians();
	}

	public double getTurnAngle() {
		return Units.radiansToDegrees(getTurningRadians());
	}

	/**
	 * Returns the current velocity of the module.
	 *
	 * @return The current velocity of the module.
	 */
	public double getVelocity() {
		if (RobotBase.isReal())
			return mDriveMotor.getSelectedSensorVelocity() * Constants.ModuleConstants.kDriveEncoderDistancePerPulse
					* 10;
		else
			return driveEncoder.getVelocity().ccw().rad();
		// return outputState.speedMetersPerSecond;
	}

	/**
	 * Returns the current state of the module.
	 *
	 * @return The current state of the module.
	 */
	public SwerveModuleState getState() {
		return new SwerveModuleState(getVelocity(), new Rotation2d(getTurningRadians()));
	}

	/**
	 * Sets the desired state for the module.
	 *
	 * @param state Desired state with speed and angle.
	 */
	public void setDesiredState(SwerveModuleState state) {
		outputState = SwerveModuleState.optimize(state, new Rotation2d(getTurningRadians()));

		// Calculate the drive output from the drive PID controller.
		m_driveOutput = m_drivePIDController.calculate(
				getVelocity(), outputState.speedMetersPerSecond);

		double driveFeedforward = m_driveFeedforward.calculate(state.speedMetersPerSecond);

		// Calculate the turning motor output from the turning PID controller.
		m_turnOutput = m_turningPIDController.calculate(getTurningRadians(), outputState.angle.getRadians());

		double turnFeedforward = m_turnFeedforward.calculate(m_turningPIDController.getSetpoint().velocity);

		// driveOutput=0;
		// System.out.println("Turn PID Output: " + turnOutput);
		// m_driveOutput = Math.signum(m_driveOutput) *
		// Math.min(Math.abs(m_driveOutput), 0.1);
		// m_turnOutput = Math.signum(m_turnOutput) * Math.min(Math.abs(m_turnOutput),
		// 0.4);

		mDriveMotor.set(ControlMode.PercentOutput, m_driveOutput + driveFeedforward);
		mTurningMotor.set(ControlMode.PercentOutput, m_turnOutput);
	}

	@Override
	public void periodic() {

		// Simulate module instead of reading actual values
		if (AbstractRobot.isSimulation()) {
			// Amount of time that each cycle of the RoboRIO runs for
			double periodicPeriod = 1 / AbstractRobot.get().getPeriodicPerSecond();

			moduleRotationSimModel.setInputVoltage(
					m_turnOutput / kMaxModuleAngularSpeedRadiansPerSecond * RobotController.getBatteryVoltage());
			moduleThrottleSimModel.setInputVoltage(m_driveOutput / Constants.DriveConstants.kMaxSpeedMetersPerSecond
					* RobotController.getBatteryVoltage());

			moduleRotationSimModel.update(periodicPeriod);
			moduleThrottleSimModel.update(periodicPeriod);


			// Simulate distances using velocities
			simTurnEncoderDistance += moduleRotationSimModel.getAngularVelocityRadPerSec() * periodicPeriod;
			simThrottleEncoderDistance += moduleThrottleSimModel.getAngularVelocityRadPerSec() * periodicPeriod;

			turnEncoder.setAngle(AbsoluteAngle.rad(simTurnEncoderDistance));
			turnEncoder.setVelocity(
					AbsoluteAngle.rad(moduleRotationSimModel.getAngularVelocityRadPerSec() / kTurningMotorGearRatio));

			driveEncoder.setVelocity(CCWAngle.rad(moduleThrottleSimModel.getAngularVelocityRadPerSec()));
		}
	}
}