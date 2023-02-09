package com.swrobotics.robot.subsystems.vision;

import java.io.IOException;
import java.util.ArrayList;

import org.photonvision.PhotonCamera;
import org.photonvision.RobotPoseEstimator;
import org.photonvision.SimVisionSystem;
import org.photonvision.RobotPoseEstimator.PoseStrategy;

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
    // Drive to use for odometry
    private final DrivetrainSubsystem drive;

    // Create cameras run through RPi
    private final PhotonCamera frontCam = new PhotonCamera("Front");
    private final Transform3d frontCamTransform = new Transform3d(
        new Translation3d(
            Units.inchesToMeters(0),  // X
            Units.inchesToMeters(10),  // Y
            Units.inchesToMeters(20)), // Z
        new Rotation3d()); // Camera is facing perfectly forward

    private final PhotonCamera backCam = new PhotonCamera("Back");
    private final Transform3d backCamTransform = new Transform3d(
        new Translation3d(
            Units.inchesToMeters(0),  // X
            Units.inchesToMeters(10),  // Y
            Units.inchesToMeters(20)), // Z
        new Rotation3d(
            0,
            0,
            Math.PI
        )); // Camera is facing perfectly forward

    // Simulate cameras
    private SimVisionSystem frontSim;
    private SimVisionSystem backSim;

    // Create a pose estimator to use multiple tags + multiple cameras to figure out where the robot is
    private final RobotPoseEstimator poseEstimator;

    public Photon(RobotContainer robot) {
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
        cameras.add(new Pair<>(backCam, backCamTransform));

        poseEstimator = new RobotPoseEstimator(layout, PoseStrategy.LOWEST_AMBIGUITY, cameras);

        // Simulate cameras
        if (RobotBase.isSimulation()) {
            frontSim = new SimVisionSystem("Front", 70, frontCamTransform, 9000, 320, 240, 0); // FIXME: FOV
            backSim = new SimVisionSystem("Back", 70, backCamTransform, 9000, 320, 240, 0); // FIXME: FOV

            frontSim.addVisionTargets(layout);
            backSim.addVisionTargets(layout);

            drive.showApriltags(layout);
        }
    }

    @Override
    public void simulationPeriodic() {

    }

    @Override
    public void periodic() {
        if (RobotBase.isSimulation()) {
            frontSim.processFrame(drive.getPose());
            backSim.processFrame(drive.getPose());
        }

        // Update estimator with odometry readings
        poseEstimator.setReferencePose(drive.getPose());

        // Read cameras and estimate poses
        var estimatedPose = poseEstimator.update();

        // No targets found / impossible to estimate
        if (estimatedPose.isEmpty()) {
            return;
        }

        if (estimatedPose.get().getFirst() == null) {
            return;
        }

        // System.out.println("Targets found!" + frontCam.getLatestResult().targets.size());

        // System.out.println(estimatedPose.get().getFirst());

        // // Update drive with estimated pose
        drive.resetPose(estimatedPose.get().getFirst().toPose2d());
    }



}