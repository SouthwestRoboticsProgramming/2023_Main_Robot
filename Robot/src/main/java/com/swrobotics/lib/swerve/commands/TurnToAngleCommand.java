package com.swrobotics.lib.swerve.commands;

import java.util.function.Supplier;

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
    private final Supplier<Angle> angle;
    private final boolean robotRelative;

    public TurnToAngleCommand(RobotContainer robot, Supplier<Angle> angle, boolean robotRelative) {
        drive = robot.drivetrainSubsystem;
        this.angle = angle;
        this.robotRelative = robotRelative;

        pid = new ProfiledPIDController(
            10, 2, 0,
            new TrapezoidProfile.Constraints(6.28, 3.14));
        pid.enableContinuousInput(-Math.PI, Math.PI);

        pid.setTolerance(0.1);
    }

    protected Angle getTargetAngle() {
        return angle.get();
    }

    protected Rotation2d getTarget() {
        return angle.get().ccw().rotation2d();
    }

    protected ProfiledPIDController getPID() {
        return pid;
    }

    @Override
    public void initialize() {
        pid.reset(drive.getPose().getRotation().getRadians());
    }

    @Override
    public void execute() {
        // Update the target
        Rotation2d target = getTarget();

        if (robotRelative) {
            target = target.plus(drive.getPose().getRotation());
        }

        drive.setTargetRotation(new Rotation2d(
            pid.calculate(
                drive.getPose().getRotation().getRadians(),
                target.getRadians()
            )
        ));
    }

    @Override
    public boolean isFinished() {
        return Math.abs(getTarget().minus(drive.getPose().getRotation()).getRadians()) < ANGLE_TOLERANCE_RAD;
    }
}
