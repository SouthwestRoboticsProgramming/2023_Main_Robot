package com.swrobotics.lib.swerve.commands;

import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.drive.Pathfinder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;

import java.util.List;

public final class PathfindToPointCommand extends CommandBase {
    // Speed at which the robot tries to go
    private static final double VELOCITY = 1.0;

    // Position tolerance in meters, must be larger than pathfinding tile
    private static final double TOLERANCE = 0.175;

    private final DrivetrainSubsystem drive;
    private final Pathfinder finder;

    private final Vec2d goal;
    private boolean finished;

    public PathfindToPointCommand(RobotContainer robot, Vec2d goal) {
        drive = robot.drivetrainSubsystem;
        finder = robot.pathfinder;
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
        Vec2d currentPosition = new Vec2d(
                currentPose.getX(),
                currentPose.getY()
        );

        Vec2d target = null;
        if (!finder.isPathTargetValid()) {
            System.err.println("Path target is incorrect, waiting for good path");

            // Drive directly to goal while waiting
            target = goal;
        } else {
            List<Vec2d> currentPath = finder.getPath(); // Update path with the new, valid path

            // Because of latency, the starting point of the path can be significantly
            // behind the actual location
            // With the predefined path there is effectively infinite latency so this is very important
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

            // If we aren't near the path at all, we need to wait for the pathfinder to make a valid path
            if (target == null) {
                System.err.println("Waiting for pathfinder to catch up");
                drive.setChassisSpeeds(new ChassisSpeeds(0, 0, 0));
                return;
            }
        }

        // We are finished if within tolerance to final target
        if (new Vec2d(currentPosition).sub(goal).magnitudeSq() < TOLERANCE * TOLERANCE)
            finished = true;

        // Find normal vector towards target
        double deltaX = target.x - currentPosition.x;
        double deltaY = target.y - currentPosition.y;
        double len = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        deltaX /= len; deltaY /= len;

        // Scale vector by velocity
        deltaX *= VELOCITY;
        deltaY *= VELOCITY;

        // Move
        drive.setTargetTranslation(new Translation2d(deltaX, deltaY), true);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
