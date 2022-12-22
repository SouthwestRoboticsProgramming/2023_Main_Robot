package com.swrobotics.lib.swerve.commands;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class TurnToAngleCommand extends CommandBase {

    private static final double ANGLE_TOLERANCE_RAD = 0.05;

    private final DrivetrainSubsystem drive;
    private final ProfiledPIDController pid;
    private final Rotation2d target;
    
    public TurnToAngleCommand(RobotContainer robot, Angle angle, boolean robotRelative) {
        target = angle.ccw().rotation2d();
        drive = robot.m_drivetrainSubsystem;
        pid = new ProfiledPIDController(
            2, 0, 0, 
            new TrapezoidProfile.Constraints(6.28, 3.14));
        // pid.enableContinuousInput(-Math.PI, Math.PI);

        pid.setTolerance(0.1);
    }

    @Override
    public void initialize() {
        pid.reset(drive.getPose().getRotation().getRadians());
    }

    @Override
    public void execute() {
        drive.combineChassisSpeeds(
            new ChassisSpeeds(0, 0, 
            -pid.calculate(
                drive.getPose().getRotation().getRadians(),
                target.getRadians()))
        );
    }


    @Override
    public boolean isFinished() {
        System.out.println("Current: " + drive.getPose().getRotation().getDegrees());
        System.out.println("Target: " + target.getDegrees());
        return Math.abs(target.minus(drive.getPose().getRotation()).getRadians()) < ANGLE_TOLERANCE_RAD;
    }
}
