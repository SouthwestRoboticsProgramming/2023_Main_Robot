package com.swrobotics.robot.positions;

import com.swrobotics.lib.swerve.commands.TurnToAngleCommand;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.RobotContainer;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

import java.util.function.Supplier;

public final class TurnWithArmSafetyCommand extends TurnToAngleCommand {
    private enum State {
        UNCONSTRAINED,
        LOWER_LIMITED,
        UPPER_LIMITED
    }

    private static final double FIELD_HEIGHT = 8.0035;

    private final RobotContainer robot;
    private final Translation2d driveTarget;
    private State prevState;

    public TurnWithArmSafetyCommand(
            RobotContainer robot, Supplier<Angle> angle, Vec2d driveTarget) {
        super(robot, angle, false);
        this.robot = robot;
        this.driveTarget = new Translation2d(driveTarget.x, driveTarget.y);

        prevState = null;
    }

    private double distanceToWall(Translation2d tx) {
        return Math.min(tx.getY(), FIELD_HEIGHT - tx.getY());
    }

    @Override
    public void execute() {
        Pose2d currentPose = robot.drivetrainSubsystem.getPose();

        // Determine whether the walls are relevant and which one
        double armExtension =
                Math.max(
                        robot.arm.getCurrentPose().getEndPosition().getX(),
                        robot.arm.getTargetPose().getEndPosition().getX());
        double distToWall = distanceToWall(currentPose.getTranslation());
        if (driveTarget != null) distToWall = Math.min(distToWall, distanceToWall(driveTarget));

        // Get the current constraint state
        State state = State.UNCONSTRAINED;
        if (distToWall < armExtension) {
            state =
                    currentPose.getY() < FIELD_HEIGHT / 2
                            ? State.LOWER_LIMITED
                            : State.UPPER_LIMITED;
        }

        // Get current and target angles
        Angle current =
                CCWAngle.rad(robot.drivetrainSubsystem.getPose().getRotation().getRadians());
        Angle target = getTargetAngle();

        // Angle wrapping magic - we set the wrap point towards the
        // edge to avoid, and disable continuity, so the PID won't
        // make the robot pass through it
        PIDController pid = getPID();
        double currentIn, targetIn;
        switch (state) {
            case UNCONSTRAINED:
                pid.enableContinuousInput(0, 2 * Math.PI);
                currentIn = current.ccw().wrapDeg(0, 360).rad();
                targetIn = target.ccw().wrapDeg(0, 360).rad();
                break;
            case LOWER_LIMITED:
                pid.disableContinuousInput();
                currentIn = current.ccw().wrapDeg(-90, 270).rad();
                targetIn = target.ccw().wrapDeg(-90, 270).rad();
                break;
            case UPPER_LIMITED:
                pid.disableContinuousInput();
                currentIn = current.ccw().wrapDeg(90, 450).rad();
                targetIn = target.ccw().wrapDeg(90, 450).rad();
                break;
            default:
                throw new AssertionError();
        }

        // Reset PID if state has changed
        if (state != prevState) pid.reset();
        prevState = state;

        // Turn
        double pidOut = pid.calculate(currentIn, targetIn);
        pidOut = MathUtil.clamp(pidOut, -MAX_ROTATIONAL_VEL, MAX_ROTATIONAL_VEL);
        robot.drivetrainSubsystem.setTargetRotation(new Rotation2d(pidOut));
    }
}
