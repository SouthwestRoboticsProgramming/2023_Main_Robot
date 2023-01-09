package com.swrobotics.robot.subsystems.vision;

import com.swrobotics.lib.net.NTBoolean;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Limelight extends SubsystemBase {
    private static final NTBoolean LIGHTS_ON = new NTBoolean("Limelight/Lights_On", true);
    private static final NTBoolean TARGET_FOUND = new NTBoolean("Limelight/Target found", false);

    private final NetworkTableEntry xAngle;
    private final NetworkTableEntry yAngle;
    private final NetworkTableEntry targetArea;
    private final NetworkTableEntry isValidTarget;

    private final NetworkTableEntry lightsOn;

    public Limelight() {
        LIGHTS_ON.set(true); // Default to lights on so as not to forget

        TARGET_FOUND.setTemporary(); // Logging

        NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
        xAngle = table.getEntry("tx");
        yAngle = table.getEntry("ty");
        targetArea = table.getEntry("ta");
        isValidTarget = table.getEntry("tv");

        lightsOn = table.getEntry("ledMode");

        // Lights are on by default but can be changed any time
        LIGHTS_ON.onChange(() -> setLights(LIGHTS_ON.get()));
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
        int value = 1;     // 1 is off
        if (on) value = 3; // 3 is on
        lightsOn.setNumber(value);
        LIGHTS_ON.set(on);
    }

    @Override
    public void periodic() {
        // Update log with data
        TARGET_FOUND.set(targetFound());
    }
}