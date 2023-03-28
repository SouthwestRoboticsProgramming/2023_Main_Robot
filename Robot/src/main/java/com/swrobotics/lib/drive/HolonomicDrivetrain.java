package com.swrobotics.lib.drive;

import com.swrobotics.lib.field.FieldInfo;
import com.swrobotics.lib.gyro.Gyroscope;

// TODO: Determine whether this is needed
public abstract class HolonomicDrivetrain extends Drivetrain {
    public HolonomicDrivetrain(FieldInfo fieldInfo, Gyroscope gyro) {
        super(fieldInfo, gyro);
    }
}
