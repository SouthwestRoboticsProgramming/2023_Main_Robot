package com.swrobotics.robot.commands;

import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.Lights;
import com.swrobotics.robot.subsystems.Pathfinder;
import com.swrobotics.robot.subsystems.Lights.IndicatorMode;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.CommandBase;

import java.util.List;

public final class PathfindToPointCommand extends CommandBase {
    // Speed at which the robot tries to go
    private static final double VELOCITY = 0.3;

    // Position tolerance in meters, must be larger than pathfinding tile
    private static final double TOLERANCE = 0.175;

    // Angle tolerance in radians
    private static final double ANGLE_TOLERANCE = Math.toRadians(3);

    private final DrivetrainSubsystem drive;
    private final Pathfinder finder;
    private final Lights lights; // For debugging; TODO: Remove

    private final Vec2d goal;
    private final ProfiledPIDController turnPID;
    private final Rotation2d targetAngle;

    public PathfindToPointCommand(DrivetrainSubsystem drive, Pathfinder finder, Lights lights, Vec2d goal, Rotation2d targetAngle) {
        this.drive = drive;
        this.finder = finder;
        this.lights = lights;
        this.goal = goal;
        this.targetAngle = targetAngle;

        addRequirements(drive);

        turnPID = new ProfiledPIDController(
                2, 0, 0, // Seems like these work in simulator
                new TrapezoidProfile.Constraints(6.28, 3.14)
        );
        turnPID.enableContinuousInput(-Math.PI, Math.PI);
    }

    @Override
    public void initialize() {
        drive.resetPose(new Pose2d(8.2296, 8.2296/2, new Rotation2d()));
        turnPID.reset(drive.getPose().getRotation().getRadians());
    }

    @Override
    public void execute() {
        finder.setGoal(goal.x, goal.y);
        if (!finder.isPathValid()) {
            System.out.println("Path bad");
            lights.set(Lights.Color.RED);
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

            // Indicate that this is what is happening
            lights.set(IndicatorMode.FAILED);
            return;
        }

        // Find normal vector towards target
        double deltaX = target.x - currentPosition.x;
        double deltaY = target.y - currentPosition.y;
        double len = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (len < TOLERANCE) {
            System.out.println("In tolerance to target");
            // Don't move if already in tolerance (waiting for angle to be good)
            deltaX = 0;
            deltaY = 0;
        }
        deltaX /= len; deltaY /= len;

        // Scale vector by velocity
        deltaX *= VELOCITY;
        deltaY *= VELOCITY;

        // Calculate speeds
        ChassisSpeeds speeds = ChassisSpeeds.fromFieldRelativeSpeeds(
                deltaX, deltaY,
                turnPID.calculate(currentPose.getRotation().getRadians(), targetAngle.getRadians()),
                currentPose.getRotation()
        );

        // Move
        drive.setChassisSpeeds(speeds);
        lights.set(IndicatorMode.GOOD); // Indicate it is working correctly
    }

    @Override
    public boolean isFinished() {
        Pose2d currentPose = drive.getPose();
        Vec2d currentPosition = new Vec2d(
                currentPose.getX(),
                currentPose.getY()
        );

        boolean positionInTol = currentPosition.distanceToSq(goal) < TOLERANCE * TOLERANCE;
        boolean angleInTol = Math.abs(targetAngle.minus(currentPose.getRotation()).getRadians()) < ANGLE_TOLERANCE;

        return positionInTol && angleInTol;
    }
}
