package com.swrobotics.robot.subsystems.vision;

import java.io.IOException;
import java.util.ArrayList;

import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.SimVisionSystem;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;

import com.swrobotics.lib.net.NTInteger;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Photon extends SubsystemBase {
    // Telemetry log
    private final NTInteger L_TARGETS_FOUND = new NTInteger("Vision/Targets Found", 0);

    // Drive to use for odometry
    private final DrivetrainSubsystem drive;

    private static final double POWER_TOWER_HEIGHT = 11.17; // Measured from floor
    private static final double POWER_TOWER_X = 10.73; // Measured from intake center rivet

    // Create cameras run through RPi
    private final PhotonCamera frontCam = new PhotonCamera("Front");
    private final Transform3d frontCamTransform = new Transform3d(
        new Translation3d(
            Units.inchesToMeters(-5.01),  // Forward
            Units.inchesToMeters(POWER_TOWER_X),  // Left
            Units.inchesToMeters(POWER_TOWER_HEIGHT)), // Up
        new Rotation3d()); // Camera is facing perfectly forward

//    private final PhotonCamera backCam = new PhotonCamera("Back");
//    private final Transform3d backCamTransform = new Transform3d(
//        new Translation3d(
//            Units.inchesToMeters(-7.71),  // Forward
//            Units.inchesToMeters(POWER_TOWER_X),  // Left
//            Units.inchesToMeters(POWER_TOWER_HEIGHT)), // Up
//        new Rotation3d(
//            0,
//            0,
//            Math.PI
//        )); // Camera is facing perfectly backward

    // Simulate cameras
    private SimVisionSystem frontSim;
//    private SimVisionSystem backSim;

    // Create a pose estimator to use multiple tags + multiple cameras to figure out where the robot is
    // FIXME-Mason: Use PhotonPoseEstimator, RobotPoseEstimator is deprecated
    private final PhotonPoseEstimator poseEstimator;

    public Photon(RobotContainer robot) {
        L_TARGETS_FOUND.setTemporary();

        drive = robot.drivetrainSubsystem;

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

        poseEstimator = new PhotonPoseEstimator(layout, PoseStrategy.AVERAGE_BEST_TARGETS, frontCam, frontCamTransform);

        // Simulate cameras
        if (RobotBase.isSimulation()) {
            frontSim = new SimVisionSystem("Front", 70, frontCamTransform, 9000, 320, 240, 0); // FIXME: FOV
//            backSim = new SimVisionSystem("Back", 70, backCamTransform, 9000, 320, 240, 0); // FIXME: FOV

            frontSim.addVisionTargets(layout);
//            backSim.addVisionTargets(layout);

            drive.showApriltags(layout);
            drive.showCameraPoses(frontCamTransform);
        }
    }

    @Override
    public void periodic() {
        if (RobotBase.isSimulation()) {
            frontSim.processFrame(drive.getPose());
//            backSim.processFrame(drive.getPose());
        }

        L_TARGETS_FOUND.set(
            frontCam.getLatestResult().targets.size()/* +
            backCam.getLatestResult().targets.size()*/);

        // Update estimator with odometry readings
        poseEstimator.setReferencePose(drive.getPose());

        // Read cameras and estimate poses
        var estimatedPose = poseEstimator.update();

        // No targets found / impossible to estimate
        if (estimatedPose.isEmpty()) {
            return;
        }

        if (estimatedPose.get() == null) {
            return;
        }

        // System.out.println("Targets found!" + frontCam.getLatestResult().targets.size());

        // System.out.println(estimatedPose.get().getFirst());

        // // Update drive with estimated pose
        L_MOVING.set(drive.isMoving());
        if (!drive.isMoving())
            drive.resetPose(estimatedPose.get().estimatedPose.toPose2d());
    }

    private final NTEntry<Boolean> L_MOVING = new NTBoolean("Log/Moving", false).setTemporary();



}