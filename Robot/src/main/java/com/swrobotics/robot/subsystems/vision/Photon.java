package com.swrobotics.robot.subsystems.vision;

import org.photonvision.PhotonCamera;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static com.swrobotics.robot.VisionConstants.*;

import java.util.TreeMap;

import com.swrobotics.robot.PoseFilter;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;

public class Photon extends SubsystemBase {

    private static PoseFilter filter = new PoseFilter(15);

    private static final double MAX_AMBIGUITY = 0.2;

    // Tree map to map each AprilTag IDs to locations
    private final TreeMap<Integer, Pose3d> targets = new TreeMap<>();

    private final PhotonCamera camera = new PhotonCamera("Front"); // PhotonVision on Limelight must be called "Front"
    private final DrivetrainSubsystem drive;

    public Photon(DrivetrainSubsystem drive) {
        this.drive = drive;

        SmartDashboard.putBoolean("Calibrate with vision", true);
    }

    @Override
    public void periodic() {
        SmartDashboard.putBoolean("Target Found", false);

        // Don't update with blurry photos
        if (drive.isMoving()) {
            filter.get(drive.getPose());
            return;
        }

        // Don't update if there are no targets
        var results = camera.getLatestResult();
        if (!results.hasTargets()) {
            filter.get(drive.getPose());
            return;
        }
        
        // Don't update if the target was tracked poorly
        double ambiguity = results.getBestTarget().getPoseAmbiguity();
        if (ambiguity > MAX_AMBIGUITY || ambiguity < 0) {
            System.out.println("Too ambiguous");
            filter.get(drive.getPose());
            return;
        }

        SmartDashboard.putBoolean("Target Found", true);
        
        // Get the location of the camera relative to the target
        Transform3d relativeCameraPosition3d = results.getBestTarget().getBestCameraToTarget();
        Transform2d relativeCameraPosition = new Transform2d(
            relativeCameraPosition3d.getTranslation().toTranslation2d(),
            relativeCameraPosition3d.getRotation().toRotation2d());
            
            // Get the location of the target
            int targetID = results.getBestTarget().getFiducialId();
            if (!targets.containsKey(targetID)) {
                filter.get(drive.getPose());
                return;
            }

            Pose2d targetPose = targets.get(targetID).toPose2d();
            // Pose2d targetPose = DOOR_POSE.toPose2d();
            
            // Find the location of the camera relative to the world
            Pose2d cameraPose = targetPose.transformBy(relativeCameraPosition.inverse());
            Transform2d cameraRelativeToRobot = new Transform2d(CAMERA_POSITION.getTranslation().toTranslation2d(),
            CAMERA_POSITION.getRotation().toRotation2d());

            Pose2d robotPose = cameraPose.transformBy(cameraRelativeToRobot);

            robotPose = filter.get(robotPose);
            
            // drive.field.getObject("Vision Path").setPose(new Pose2d(cameraRelativeToRobot.getTranslation().times(0.5), cameraRelativeToRobot.getRotation()));
            if (SmartDashboard.getBoolean("Calibrate with vision", true)) {
            // Find the location of the robot and update the estimated position
            drive.resetPose(robotPose);
        }
    }
}
