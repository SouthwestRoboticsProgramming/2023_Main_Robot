package com.swrobotics.lib.drive.swerve.commands;

import com.swrobotics.lib.drive.swerve.Pathfinder;
import com.swrobotics.lib.drive.swerve.SwerveDrive;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.CommandBase;

import java.util.List;

public final class PathfindToPointCommand extends CommandBase {
    // Maximum speed
    private static final double VELOCITY = 1.0;

    // Position tolerance in meters
    private static final double TOLERANCE = 0.06;

    private final SwerveDrive drive;
    private final Pathfinder finder;
    private final PIDController pid;

    private Vec2d goal;
    private boolean finished;

    public PathfindToPointCommand(SwerveDrive drive, Pathfinder pathfinder, Vec2d goal) {
        this.drive = drive;
        this.finder = pathfinder;
        this.goal = goal;

        // FIXME: Tune
        pid = new PIDController(10, 0, 0);
    }

    public void setGoal(Vec2d goal) {
        this.goal = goal;
    }

    @Override
    public void initialize() {
        finished = false;
    }

    @Override
    public void execute() {
        finder.setGoal(goal.x, goal.y);

        Pose2d currentPose = drive.getPose();
        Vec2d currentPosition = new Vec2d(currentPose.getX(), currentPose.getY());

        Vec2d target = null;
        if (!finder.isPathTargetValid()) {
            // Drive directly to goal while waiting
            target = goal;
        } else {
            List<Vec2d> currentPath = finder.getPath(); // Update path with the new, valid path

            // Because of latency, the starting point of the path can be significantly
            // behind the actual location
            // With the predefined path there is effectively infinite latency so this is very
            // important
            double minDist = Double.POSITIVE_INFINITY;
            for (int i = currentPath.size() - 1; i > 0; i--) {
                Vec2d point = currentPath.get(i);
                Vec2d prev = currentPath.get(i - 1);

                double dist = currentPosition.distanceToLineSegmentSq(point, prev);

                // If the robot is closest to this line, use its endpoint as the target
                if (dist < minDist) {
                    minDist = dist;
                    target = point;
                }
            }

            // If we aren't near the path at all, we need to wait for the pathfinder to make a valid
            // path
            if (target == null) {
                System.err.println("Waiting for pathfinder to catch up");
                drive.addTranslation(new Translation2d(0, 0), true);
                return;
            }
        }

        // We are finished if within tolerance to final target
        double magToGoal = currentPosition.distanceTo(goal);
        if (magToGoal < TOLERANCE) finished = true;

        double velocity = -pid.calculate(magToGoal, 0);
        velocity = MathUtil.clamp(velocity, -VELOCITY, VELOCITY);

        // Find normal vector towards target
        double deltaX = target.x - currentPosition.x;
        double deltaY = target.y - currentPosition.y;
        double len = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        deltaX /= len;
        deltaY /= len;

        // Scale vector by velocity
        deltaX *= velocity;
        deltaY *= velocity;

        // Move
        drive.addTranslation(new Translation2d(deltaX, deltaY), true);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
