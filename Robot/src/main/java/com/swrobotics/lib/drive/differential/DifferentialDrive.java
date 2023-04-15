package com.swrobotics.lib.drive.differential;

import com.pathplanner.lib.auto.BaseAutoBuilder;
import com.pathplanner.lib.auto.RamseteAutoBuilder;
import com.swrobotics.lib.drive.Drivetrain;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.field.FieldInfo;
import com.swrobotics.lib.field.FieldSymmetry;
import com.swrobotics.lib.gyro.Gyroscope;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.MathUtil;
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;

import java.util.Map;

/**
 * Drivetrain implementation for a differential drivetrain.
 */
public class DifferentialDrive extends Drivetrain {
    private static final double IS_MOVING_TOL = 0.1;

    private enum ControlMode {
        PERCENT,
        VELOCITY
    }

    private final FeedbackMotor leftMotor;
    private final FeedbackMotor rightMotor;
    private final Encoder leftEncoder;
    private final Encoder rightEncoder;

    private ControlMode controlMode;
    private double openLoopMaxVelocity;

    private final double wheelRadius;
    private final DifferentialDriveKinematics kinematics;
    private final DifferentialDriveOdometry odometry;

    /**
     * Creates a new differential drive instance. If there are multiple
     * motors per side, the leader should be passed in here, and the
     * other motors should follow the leader.
     *
     * @param fieldInfo information about the game field
     * @param gyro gyroscope to measure orientation
     * @param leftMotor left motor, clockwise should be forward
     * @param rightMotor right motor, clockwise should be forward
     * @param wheelRadius radius of each wheel in meters
     * @param wheelSpacing spacing between the wheels horizontally in meters
     */
    public DifferentialDrive(
            FieldInfo fieldInfo,
            Gyroscope gyro,
            FeedbackMotor leftMotor,
            FeedbackMotor rightMotor,
            double wheelRadius,
            double wheelSpacing
    ) {
        super(fieldInfo, gyro);

        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        this.leftEncoder = leftMotor.getIntegratedEncoder();
        this.rightEncoder = rightMotor.getIntegratedEncoder();

        this.wheelRadius = wheelRadius;
        kinematics = new DifferentialDriveKinematics(wheelSpacing);
        odometry = new DifferentialDriveOdometry(getGyroRotation2d(), getDistance(leftEncoder), getDistance(rightEncoder));

        controlMode = ControlMode.VELOCITY;
    }

    public DifferentialDrive withOpenLoop(double maxVelocity) {
        openLoopMaxVelocity = maxVelocity;
        controlMode = ControlMode.PERCENT;
        return this;
    }

    private double getDistance(Encoder encoder) {
        return encoder.getAngle().cw().rad() * wheelRadius;
    }

    private double getVelocity(Encoder encoder) {
        return encoder.getVelocity().cw().rad() * wheelRadius;
    }

    private Rotation2d getGyroRotation2d() {
        return new Rotation2d(gyro.getAngle().ccw().rad());
    }

    @Override
    protected Pose2d getOdometryPose() {
        return odometry.getPoseMeters();
    }

    @Override
    protected void setOdometryPose(Pose2d pose) {
        odometry.resetPosition(getGyroRotation2d(), getDistance(leftEncoder), getDistance(rightEncoder), pose);
    }

    @Override
    public boolean isMoving() {
        double leftVel = getVelocity(leftEncoder);
        double rightVel = getVelocity(rightEncoder);
        return Math.abs(leftVel) > IS_MOVING_TOL || Math.abs(rightVel) > IS_MOVING_TOL;
    }

    @Override
    public void setBrakeMode(boolean brake) {
        leftMotor.setBrakeMode(brake);
        rightMotor.setBrakeMode(brake);
    }

    @Override
    protected BaseAutoBuilder createAutoBuilder(Map<String, Command> eventMap) {
        // TODO: Test, is probably wrong
        return new RamseteAutoBuilder(
                this::getOdometryPose,
                this::resetPoseInternal,
                new RamseteController(),
                kinematics,
                this::driveWheels,
                eventMap,
                fieldInfo.getSymmetry() == FieldSymmetry.LATERAL,
                this
        );
    }

    private void driveWheels(double leftMPS, double rightMPS) {
        if (controlMode == ControlMode.PERCENT) {
            leftMotor.setPercentOut(leftMPS / openLoopMaxVelocity);
            rightMotor.setPercentOut(rightMPS / openLoopMaxVelocity);
        } else {
            leftMotor.setVelocity(CCWAngle.rad(leftMPS / wheelRadius));
            rightMotor.setVelocity(CCWAngle.rad(rightMPS / wheelRadius));
        }
    }

    @Override
    protected void drive(ChassisSpeeds speeds) {
        if (Math.abs(speeds.vyMetersPerSecond) > MathUtil.EPSILON)
            DriverStation.reportWarning("Differential drive cannot drive sideways", false);

        DifferentialDriveWheelSpeeds wheels = kinematics.toWheelSpeeds(speeds);
        driveWheels(wheels.leftMetersPerSecond, wheels.rightMetersPerSecond);
    }
}
