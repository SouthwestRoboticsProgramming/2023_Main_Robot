package com.swrobotics.robot.commands;

import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.Lights;
import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.CommandBase;

import java.util.ArrayList;
import java.util.List;

// TODO: Do this in our coordinates instead of WPI
public final class FollowPathCommand extends CommandBase {
    /*
     * This path assumes:
     *   The robot starts at (0, 0), with heading 0
     *   Units are in meters
     *   +X is forward (WPI coords)
     *   +Y is left (WPI coords)
     *
     * It should drive in this shape:
     *   G
     *   ^
     *   O < O
     *       ^
     *       S
     * where S is start and G is goal, and each movement is one meter,
     * and rotate around 180 degrees.
     */
    private static final List<Vec2d> path = new ArrayList<>();
    static {
        // Basic path
//        path.add(new Vec2d(0, 0));
//        path.add(new Vec2d(0.5, 0));
//        path.add(new Vec2d(0.5, 0.5));
//        path.add(new Vec2d(1, 0.5));
//
        // Test long distance vs steps
//        path.add(new Vec2d(0, 0));
//        path.add(new Vec2d(0, 3));
//        path.add(new Vec2d(5, 3));
//        path.add(new Vec2d(5, 4));
//        path.add(new Vec2d(6, 4));
//        path.add(new Vec2d(7, 4));
//        path.add(new Vec2d(8, 4));
//        path.add(new Vec2d(9, 4));
//        path.add(new Vec2d(10, 4));

        // Path from one end of the room to the other
        path.add(new Vec2d(1.9718, 1.65183));
        path.add(new Vec2d(3.4249, 1.308));
        path.add(new Vec2d(4.485, 1.507));
        path.add(new Vec2d(5.94, 1.66));
    }
    private static final Rotation2d targetAngle = Rotation2d.fromDegrees(180);

    // Speed at which the robot tries to go
    // TODO-Mason: Might want to adjust this before running it inside house
    private static final double VELOCITY = 0.4;

    // Position tolerance in meters, must be larger than pathfinding tile
    private static final double TOLERANCE = 0.175;

    // Angle tolerance in radians
    private static final double ANGLE_TOLERANCE = Math.toRadians(3);

    private final DrivetrainSubsystem drive;
    private final Lights lights;

    private final ProfiledPIDController turnPID;
    private final Vec2d goal;

    public FollowPathCommand(DrivetrainSubsystem drive, Lights lights) {
        this.drive = drive;
        this.lights = lights;
        addRequirements(drive);

        turnPID = new ProfiledPIDController(
                2, 0, 0, // Seems like these work in simulator
                new TrapezoidProfile.Constraints(6.28, 3.14)
        );
        turnPID.enableContinuousInput(-Math.PI, Math.PI);

        goal = path.get(path.size() - 1);
    }

    @Override
    public void initialize() {
        turnPID.reset(drive.getPose().getRotation().getRadians());
    }

    @Override
    public void execute() {
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

            target = point; // FIXME: Hotfix maybe breaking?
        }

        // If we aren't near the path at all, we need to wait for the pathfinder to make a valid path
        if (target == null) {
            System.err.println("Waiting for pathfinder to catch up");
            drive.setChassisSpeeds(new ChassisSpeeds(0, 0, 0));

            // Indicate that this is what is happening
            lights.setColor(Lights.Color.RED);
            return;
        }

        // Find normal vector towards target
        double deltaX = target.x - currentPosition.x;
        double deltaY = target.y - currentPosition.y;
        double len = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (len < TOLERANCE) {
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
        lights.setColor(Lights.Color.GREEN); // Indicate it is working correctly
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
