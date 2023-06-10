package com.swrobotics.robot.subsystems.vision;

import com.swrobotics.lib.net.NTInteger;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.*;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.SimVisionSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Photon extends SubsystemBase {
    // Telemetry log
    private final NTInteger L_TARGETS_FOUND = new NTInteger("Vision/Targets Found", 0);

    // Drive to use for odometry
    private final DrivetrainSubsystem drive;

    private static final double POWER_TOWER_HEIGHT = 11.17; // Measured from floor
    private static final double POWER_TOWER_X = 10.73; // Measured from intake center rivet

    // Create cameras run through RPi
    private final PhotonCamera frontCam = new PhotonCamera("Front");
    private final Transform3d frontCamTransform =
            new Transform3d(
                    new Translation3d(
                            Units.inchesToMeters(-5.01 + 0.9985), // Forward
                            Units.inchesToMeters(POWER_TOWER_X + 0.8715), // Left
                            Units.inchesToMeters(POWER_TOWER_HEIGHT)), // Up
                    new Rotation3d(
                            0,
                            0,
                            Math.toRadians(-17.78208933))); // Camera is facing perfectly forward

    private final PhotonCamera backCam = new PhotonCamera("Back");
    private final Transform3d backCamTransform =
            new Transform3d(
                    new Translation3d(
                            Units.inchesToMeters(-5.01 + 0.9985), // Forward
                            Units.inchesToMeters(POWER_TOWER_X - 0.8715), // Left
                            Units.inchesToMeters(POWER_TOWER_HEIGHT)), // Up
                    new Rotation3d(
                            0,
                            0,
                            Math.toRadians(17.78208933))); // Camera is facing perfectly backward

    // Simulate cameras
    private SimVisionSystem frontSim;
    private SimVisionSystem backSim;

    // Create a pose estimator to use multiple tags + multiple cameras to figure out where the robot
    // is
    private final PhotonPoseEstimator frontPoseEstimator;
    private final PhotonPoseEstimator backPoseEstimator;

    private final AtomicReference<Pose2d> latestPose;

    public Photon(RobotContainer robot) {
        L_TARGETS_FOUND.setTemporary();

        drive = robot.swerveDrive;

        AprilTagFieldLayout layout;

        // Read the field layout from a file (hence try-catch)
        try {
            layout = AprilTagFields.k2023ChargedUp.loadAprilTagLayoutField();
        } catch (IOException e) {
            // Failed to load field
            e.printStackTrace();
            layout = new AprilTagFieldLayout(new ArrayList<>(), 0, 0);
        }

        ArrayList<Pair<PhotonCamera, Transform3d>> cameras = new ArrayList<>();
        cameras.add(new Pair<>(frontCam, frontCamTransform));
        //        cameras.add(new Pair<>(backCam, backCamTransform));

        frontPoseEstimator =
                new PhotonPoseEstimator(
                        layout,
                        PhotonPoseEstimator.PoseStrategy.AVERAGE_BEST_TARGETS,
                        frontCam,
                        frontCamTransform);
        backPoseEstimator =
                new PhotonPoseEstimator(
                        layout,
                        PhotonPoseEstimator.PoseStrategy.AVERAGE_BEST_TARGETS,
                        backCam,
                        backCamTransform);

        // Simulate cameras
        if (RobotBase.isSimulation()) {
            frontSim =
                    new SimVisionSystem(
                            "Front", 70, frontCamTransform, 9000, 320, 240, 0); // FIXME: FOV
            backSim =
                    new SimVisionSystem(
                            "Back", 70, backCamTransform, 9000, 320, 240, 0); // FIXME: FOV

            frontSim.addVisionTargets(layout);
            backSim.addVisionTargets(layout);

            //            drive.showApriltags(layout);
            //            drive.showCameraPoses(frontCamTransform);
        }

        latestPose = new AtomicReference<>(null);

        // Run it on seperate thread
        if (!RobotBase.isSimulation()) {
            new Thread(
                            () -> {
                                while (!Thread.interrupted()) {
                                    update();

                                    try {
                                        Thread.sleep(5);
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                }
                            })
                    .start();
        }
    }

    @Override
    public void periodic() {
        if (RobotBase.isSimulation()) update();

        Pose2d poseOut = latestPose.getAndSet(null);
        if (poseOut == null) return;

        if (!drive.isMoving() && !drive.isPathPlannerRunning()) drive.resetPose(poseOut);
    }

    public void update() {
        if (RobotBase.isSimulation()) {
            frontSim.processFrame(drive.getPose());
            //            backSim.processFrame(drive.getPose());
        }

        // L_TARGETS_FOUND.set(
        //     frontCam.getLatestResult().targets.size() +
        //     backCam.getLatestResult().targets.size());

        // Update estimator with odometry readings
        frontPoseEstimator.setReferencePose(drive.getPose());
        backPoseEstimator.setReferencePose(drive.getPose());

        // Read cameras and estimate poses
        var estimatedPoseFront = frontPoseEstimator.update();
        var estimatedPoseBack = backPoseEstimator.update();

        if (estimatedPoseFront.isEmpty() && estimatedPoseBack.isEmpty()) {
            latestPose.set(null);
            return;
        }

        Pose2d poseOut;
        if (estimatedPoseBack.isEmpty()) {
            poseOut = estimatedPoseFront.get().estimatedPose.toPose2d();
        } else if (estimatedPoseFront.isEmpty()) {
            poseOut = estimatedPoseBack.get().estimatedPose.toPose2d();
        } else {
            // Average
            Pose2d frontPose = estimatedPoseFront.get().estimatedPose.toPose2d();
            Pose2d backPose = estimatedPoseBack.get().estimatedPose.toPose2d();

            double avgRot =
                    new Vec2d(CCWAngle.rad(frontPose.getRotation().getRadians()), 1)
                            .add(new Vec2d(CCWAngle.rad(backPose.getRotation().getRadians()), 1))
                            .angle()
                            .ccw()
                            .rad();

            poseOut =
                    new Pose2d(
                            (frontPose.getX() + backPose.getX()) / 2,
                            (frontPose.getY() + backPose.getY()) / 2,
                            new Rotation2d(avgRot));
        }

        // System.out.println("Targets found!" + frontCam.getLatestResult().targets.size());

        // System.out.println(estimatedPose.get().getFirst());

        // // Update drive with estimated pose
        latestPose.set(poseOut);
    }
}
