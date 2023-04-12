package com.swrobotics.robot.subsystems.vision;

import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTDoubleArray;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Limelight extends SubsystemBase {
    public static final int CONE_NODE_PIPELINE = 0;
    public static final int CONE_PIPELINE = 1;
    public static final int CUBE_PIPELINE = 2;

    private static final NTBoolean LIGHTS_ON = new NTBoolean("Limelight/Lights_On", true);
    private static final NTBoolean TARGET_FOUND = new NTBoolean("Limelight/Target found", false);

    private static final NTBoolean DRIVER_MODE = new NTBoolean("Limelight/Driver_Mode", false);

    private final NetworkTableEntry xAngle;
    private final NetworkTableEntry yAngle;

    private final NetworkTableEntry targetArea;
    private final NetworkTableEntry isValidTarget;

    private final NetworkTableEntry crop;
    public NTDoubleArray UPPER_BOX = new NTDoubleArray("LIMELIGHT_AUTO_AIM", -1, 1, -1, 1);
    public NTDoubleArray LOWER_BOX = new NTDoubleArray("LIMELIGHT_AUTO_AIM", -1, 1, -1, 1);

    private final NetworkTableEntry driverModeOn;

    private final NetworkTableEntry lightsOn;

    private final NetworkTableEntry currentPipeline;

    public Limelight() {
        LIGHTS_ON.set(true); // Default to lights on so as not to forget
        DRIVER_MODE.set(false); // Same As Above
        TARGET_FOUND.setTemporary(); // Logging

        NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
        xAngle = table.getEntry("tx");
        yAngle = table.getEntry("ty");
        targetArea = table.getEntry("ta");
        isValidTarget = table.getEntry("tv");

        driverModeOn = table.getEntry("camMode");
        currentPipeline = table.getEntry("pipeline");

        lightsOn = table.getEntry("ledMode");
        crop = table.getEntry("crop");

        // Lights are on by default but can be changed any time
        LIGHTS_ON.onChange(() -> setLights(LIGHTS_ON.get()));

        // Driver Mode is Off By Default
        DRIVER_MODE.onChange(() -> setDriverMode(DRIVER_MODE.get()));
    }

    public Rotation2d getXAngle() {
        return Rotation2d.fromDegrees(xAngle.getDouble(0.0));
    }

    public Rotation2d getYAngle() {
        return Rotation2d.fromDegrees(yAngle.getDouble(0.0));
    }

    public double getArea() {
        return targetArea.getDouble(0.0);
    }

    public boolean targetFound() {
        return isValidTarget.getBoolean(false);
        // return isValidTarget.getDouble(0.0) == 1.0; In case the first option doesn't work
    }

    public void setLights(boolean on) {
        int value = 1; // 1 is off
        if (on) value = 3; // 3 is on
        lightsOn.setNumber(value);
        LIGHTS_ON.set(on);
    }

    @Override
    public void periodic() {
        // Update log with data
        TARGET_FOUND.set(targetFound());
    }

    // As Per: https://docs.limelightvision.io/en/latest/networktables_api.html#camera-controls
    // This Should Work
    public void setCrop(double[] crops) {

        // Java Should Have Compile Time Array Length Verification
        if (crops.length != 4) {
            throw new IllegalArgumentException("Crops Must Have 4 Values");
        }
        crop.setDoubleArray(crops);
    }

    public void setDriverMode(boolean on) {
        int value = 0;
        if (on) value = 1;

        driverModeOn.setNumber(value);
    }

    public void setPipeline(int pipeline) {
        if (pipeline < 0 || pipeline > 9) {
            throw new IllegalArgumentException("Pipeline must be between 0 and 9");
        }

        currentPipeline.setNumber(pipeline);
    }
}
