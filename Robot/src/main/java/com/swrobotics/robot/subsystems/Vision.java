package com.swrobotics.robot.subsystems;

import org.photonvision.PhotonCamera;
import org.photonvision.SimVisionSystem2022;
import org.photonvision.SimVisionTarget;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static com.swrobotics.robot.VisionConstants.*;

import java.util.TreeMap;

public class Vision extends SubsystemBase {

    private static final double MAX_AMBIGUITY = 0.2;

    // Tree map to map each AprilTag IDs to locations
    private final TreeMap<Integer, Pose3d> targets = new TreeMap<>();

    // SimVisionSystem acts as a replacement for PhotonVision server
    private final SimVisionSystem2022 simulated;

    private final PhotonCamera camera = new PhotonCamera("Front"); // PhotonVision on Limelight must be called "Front"
    private final DrivetrainSubsystem drive;

    public Vision(DrivetrainSubsystem drive) {
        this.drive = drive;

        // Map target locations to their AprilTag IDs
        targets.put(DOOR_ID, DOOR_POSE);
        targets.put(WINDOW_ID, WINDOW_POSE);

        simulated = new SimVisionSystem2022("Front", CAMERA_DIAG_FOV, CAMERA_POSITION, 10, CAMERA_RESOLUTION[0],
                CAMERA_RESOLUTION[1], PIPELINE_MIN_TARGET_AREA);

        simulated.addSimVisionTarget(DOOR_TARGET);
        simulated.addSimVisionTarget(WINDOW_TARGET);
    }

    @Override
    public void periodic() {
        var results = camera.getLatestResult();
        if (!results.hasTargets()) { return; }

        // If the target was racked really poorly, don't use it
        double ambiguity = results.getBestTarget().getPoseAmbiguity();
        if (ambiguity > MAX_AMBIGUITY || ambiguity < 0) { return; } 

        // Get the location of the camera relative to the target
        Transform3d relativeCameraPosition3d = results.getBestTarget().getBestCameraToTarget();
        Transform2d relativeCameraPosition = new Transform2d(relativeCameraPosition3d.getTranslation().toTranslation2d(), relativeCameraPosition3d.getRotation().toRotation2d());
        
        // Get the location of the target
        int targetID = results.getBestTarget().getFiducialId();
        if (!targets.containsKey(targetID)) { return; }
        Pose2d targetPose = targets.get(targetID).toPose2d();

        // Find the location of the camera relative to the world
        Pose2d cameraPose = targetPose.transformBy(relativeCameraPosition.inverse());
        Transform2d cameraRelativeToRobot = new Transform2d(CAMERA_POSITION.getTranslation().toTranslation2d(), CAMERA_POSITION.getRotation().toRotation2d());
        
        // Find the location of the robot and update the estimated position
        // drive.resetPose(cameraPose.transformBy(cameraRelativeToRobot));
    }

    @Override
    public void simulationPeriodic() {
        // Run calculations to figure out what targets the camera would be able to see
        simulated.processFrame(drive.getPose());
    }
}
