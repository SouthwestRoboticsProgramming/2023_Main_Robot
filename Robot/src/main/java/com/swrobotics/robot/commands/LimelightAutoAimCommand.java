package com.swrobotics.robot.commands;

import com.swrobotics.lib.drive.swerve.SwerveDrive;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.vision.Limelight;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class LimelightAutoAimCommand extends CommandBase {
    private static final NTDouble AIM_TOLERANCE = new NTDouble("AUTO_AIM", 1);
    private static final double MAX_ROT_VEL = Math.PI / 2;
    private final SwerveDrive swerveDrive;
    private final Limelight limelight;
    private final PIDController pidController;

    public LimelightAutoAimCommand(
            DrivetrainSubsystem swerveDrive, Limelight limelight, int pipeline) {
        this.swerveDrive = swerveDrive;

        this.limelight = limelight;
        limelight.setPipeline(pipeline);
        pidController = new PIDController(10, 2, 0);
        pidController.enableContinuousInput(-Math.PI, Math.PI);

        pidController.setTolerance(0.1);
    }

    @Override
    public void initialize() {
        pidController.reset();
    }

    @Override
    public void execute() {
        // Gets the Output of the PID Algorithim and Clamps it to the Max Angular Velo
        double ClampedPidOutput =
                MathUtil.clamp(
                        pidController.calculate(getAngleError(), getCurrentAngle()),
                        -MAX_ROT_VEL,
                        MAX_ROT_VEL);

        setRotation(ClampedPidOutput);
    }

    @Override
    public boolean isFinished() {
        return Math.abs(getAngleError()) < AIM_TOLERANCE.get();
    }

    private double getAngleError() {
        return limelight.getXAngle().getRadians();
    }

    private double getCurrentAngle() {
        return swerveDrive.getPose().getRotation().getRadians();
    }

    // Input Here Should be Clamped to a velo
    private void setRotation(double rotation) {
        swerveDrive.addRotation(new Rotation2d(rotation));
    }
}
