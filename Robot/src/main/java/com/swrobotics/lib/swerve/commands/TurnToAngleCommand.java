package com.swrobotics.lib.swerve.commands;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class TurnToAngleCommand extends CommandBase {

    private static final double ANGLE_TOLERANCE_RAD = 0.05;

    private final DrivetrainSubsystem drive;
    private final ProfiledPIDController pid;
    private Rotation2d target;
    
    public TurnToAngleCommand(RobotContainer robot, Angle angle, boolean robotRelative) {
        drive = robot.drivetrainSubsystem;
        target = angle.ccw().rotation2d();
        if (robotRelative) {
            target.plus(drive.getGyroscopeRotation());
        }
        pid = new ProfiledPIDController(
            10, 2, 0, 
            new TrapezoidProfile.Constraints(6.28, 3.14));
        pid.enableContinuousInput(-Math.PI, Math.PI);

        pid.setTolerance(0.1);
    }

    @Override
    public void initialize() {
        pid.reset(drive.getPose().getRotation().getRadians());
    }

    @Override
    public void execute() {
        drive.setTargetRotation(new Rotation2d(
            pid.calculate(
                drive.getPose().getRotation().getRadians(),
                target.getRadians()
            )
        ));
    }


    @Override
    public boolean isFinished() {
        System.out.println("Current: " + drive.getPose().getRotation().getDegrees());
        System.out.println("Target: " + target.getDegrees());
        System.out.println("Output " + pid.calculate(drive.getPose().getRotation().getRadians(), target.getRadians()));
        return Math.abs(target.minus(drive.getPose().getRotation()).getRadians()) < ANGLE_TOLERANCE_RAD;
    }
}
