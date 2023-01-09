package com.swrobotics.lib.swerve.commands;

import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.Pathfinder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;

import java.util.List;

public final class PathfindToPointCommand extends CommandBase {
    // Speed at which the robot tries to go
    private static final double VELOCITY = 0.3;

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

        addRequirements(drive.DRIVE_SUBSYSTEM);

        finished = false;
    }

    @Override
    public void execute() {
        finder.setGoal(goal.x, goal.y);
        if (!finder.isPathValid()) {
            System.out.println("Path bad");
            return;
        }
        List<Vec2d> path = finder.getPath();

        Pose2d currentPose = drive.getPose();
        Vec2d currentPosition = new Vec2d(
                currentPose.getX(),
                currentPose.getY()
        );

        // Because of latency, the starting point of the path can be significantly
        // behind the actual location
        // With the predefined path there is effectively infinite latency so this is very important
        Vec2d target = null;
        for (int i = path.size() - 1; i > 0; i--) {
            Vec2d point = path.get(i);
            Vec2d prev = path.get(i - 1);

            double dist = currentPosition.distanceToLineSegmentSq(point, prev);

            // If the robot is close enough to the line, use its endpoint as the target
            if (dist < TOLERANCE * TOLERANCE) {
                target = point;
                break;
            }
        }

        // If we aren't near the path at all, we need to wait for the pathfinder to make a valid path
        if (target == null) {
            System.err.println("Waiting for pathfinder to catch up");
            drive.setChassisSpeeds(new ChassisSpeeds(0, 0, 0));
            return;
        }

        // Find normal vector towards target
        double deltaX = target.x - currentPosition.x;
        double deltaY = target.y - currentPosition.y;
        double len = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (len < TOLERANCE) {
            finished = true;
        }
        deltaX /= len; deltaY /= len;

        // Scale vector by velocity
        deltaX *= VELOCITY;
        deltaY *= VELOCITY;

        // Calculate speeds
        ChassisSpeeds speeds = ChassisSpeeds.fromFieldRelativeSpeeds(
                deltaX, deltaY,
                0.0,
                currentPose.getRotation()
        );

        // Move
        drive.combineChassisSpeeds(speeds);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
