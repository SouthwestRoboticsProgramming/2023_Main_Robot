package com.swrobotics.robot.subsystems.drive;

import com.swrobotics.lib.net.NTDouble;

/** Describes the IDs of a swerve module. This is used to allow swerve modules to be hot-swapped */
public class SwerveModuleInfo {
    public final String name;
    public final int driveMotorID;
    public final int turnMotorID;
    public final int encoderID;
    public final NTDouble offset;

    public SwerveModuleInfo(
            String name, int driveMotorID, int turnMotorID, int encoderID, double offset) {
        this.name = name;
        this.driveMotorID = driveMotorID;
        this.turnMotorID = turnMotorID;
        this.encoderID = encoderID;
        this.offset = new NTDouble("Swerve/Modules/" + name + "/Offset Degrees", offset);
    }
}
