package com.swrobotics.lib.drive.swerve.commands;

import com.swrobotics.lib.drive.swerve.SwerveDrive;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.MathUtil;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.CommandBase;

import java.util.function.Supplier;

public class TurnToAngleCommand extends CommandBase {
    private static final double ANGLE_TOLERANCE_RAD = 0.05;
    public static final double MAX_ROTATIONAL_VEL = Math.PI / 2;

    private final SwerveDrive drive;
    private final PIDController pid;
    private Supplier<Angle> angle;
    private final boolean robotRelative;

    public TurnToAngleCommand(SwerveDrive drive, Supplier<Angle> angle, boolean robotRelative) {
        this.drive = drive;
        this.angle = angle;
        this.robotRelative = robotRelative;

        pid = new PIDController(5, 2, 0);
        pid.enableContinuousInput(-Math.PI, Math.PI);

        pid.setTolerance(0.1);
    }

    public void setAngleSupplier(Supplier<Angle> supplier) {
        this.angle = supplier;
    }

    protected Angle getTargetAngle() {
        return angle.get();
    }

    protected Rotation2d getTarget() {
        return angle.get().ccw().rotation2d();
    }

    protected PIDController getPID() {
        return pid;
    }

    @Override
    public void initialize() {
        pid.reset();
    }

    @Override
    public void execute() {
        // Update the target
        Rotation2d target = getTarget();

        if (robotRelative) {
            target = target.plus(drive.getPose().getRotation());
        }

        drive.addRotation(
                new Rotation2d(
                        MathUtil.clamp(
                                pid.calculate(
                                        drive.getPose().getRotation().getRadians(),
                                        target.getRadians()),
                                -MAX_ROTATIONAL_VEL,
                                MAX_ROTATIONAL_VEL)));
    }

    @Override
    public boolean isFinished() {
        return Math.abs(getTarget().minus(drive.getPose().getRotation()).getRadians())
                < ANGLE_TOLERANCE_RAD;
    }
}
