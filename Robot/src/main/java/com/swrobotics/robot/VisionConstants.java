package com.swrobotics.robot;

import java.util.TreeMap;

import org.photonvision.SimVisionTarget;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;

public class VisionConstants {
    
    public static final Transform3d CAMERA_POSITION = new Transform3d(
            new Translation3d(0, 0, 0.2), // X, Y, Z
            new Rotation3d(0, 0, 0) // Pitch, Yaw, Roll
    );

    public static final int[] CAMERA_RESOLUTION = new int[] { 320, 240 }; // Width, height
    public static final double CAMERA_DIAG_FOV = 75.6; // Degrees

    public static final double PIPELINE_MIN_TARGET_AREA = 10.0; // Square pixels


    /* Vision targets */
    private static final double ROOM_WIDTH = Units.inchesToMeters(823.0);
    private static final double ROOM_HEIGHT = Units.inchesToMeters(125.0);

    // private static final double ROOM_WIDTH = 5;
    // private static final double ROOM_HEIGHT = 5;

    public static final double TARGET_WIDTH = Units.inchesToMeters(8.5); // FIXME: Measure
    public static final double TARGET_HEIGHT = Units.inchesToMeters(11.0); // FIXME: Measure

    public static final Pose3d DOOR_POSE = new Pose3d(ROOM_WIDTH, ROOM_HEIGHT / 2, 0.2, new Rotation3d(0, 0, Math.PI));
    public static final int DOOR_ID = 0;

    public static final SimVisionTarget DOOR_TARGET = new SimVisionTarget(
        DOOR_POSE, TARGET_WIDTH, TARGET_HEIGHT, DOOR_ID);

    public static final Pose3d WINDOW_POSE = new Pose3d(0.0, ROOM_HEIGHT / 2, 0.2, new Rotation3d(0.0, 0.0, 0.0));
    public static final int WINDOW_ID = 1;

    public static final SimVisionTarget WINDOW_TARGET = new SimVisionTarget(
        WINDOW_POSE, TARGET_WIDTH, TARGET_HEIGHT, WINDOW_ID);


    private VisionConstants() {
        throw new AssertionError();
    }
}
