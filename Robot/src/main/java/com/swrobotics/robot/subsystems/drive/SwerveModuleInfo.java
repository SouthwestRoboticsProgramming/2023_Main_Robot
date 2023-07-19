package com.swrobotics.robot.subsystems.drive;

import com.swrobotics.lib.net.NTAngle;
import com.swrobotics.robot.config.CANAllocation;

/** Describes the IDs of a swerve module. This is used to allow swerve modules to be hot-swapped */
public class SwerveModuleInfo {
    public final String name;
    public final int driveMotorID;
    public final int turnMotorID;
    public final int encoderID;
    public final NTAngle offset;

    public SwerveModuleInfo(
            String name, CANAllocation.SwerveIds ids, NTAngle offset) {
        this.name = name;
        this.driveMotorID = ids.drive;
        this.turnMotorID = ids.turn;
        this.encoderID = ids.encoder;
        this.offset = offset;
    }
}
