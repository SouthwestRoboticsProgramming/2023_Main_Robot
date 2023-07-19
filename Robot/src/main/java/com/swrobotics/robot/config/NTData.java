package com.swrobotics.robot.config;

import com.swrobotics.lib.net.NTAngle;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTVec2d;

/**
 * Class to store all tunable NetworkTables values in one place, to make it
 * easier to hardcode the defaults in case of data loss.
 *
 * Temporary values should not be here, this class is only for persistent data.
 */
public final class NTData {
    public static final NTDouble INPUT_SPEED_RATE_LIMIT = new NTDouble("Input/Speed Slew Limit", 20);
    public static final NTDouble INPUT_ARM_TRANSLATION_RATE = new NTDouble("Input/Arm/Nudge Translation Rate", 1);
    public static final NTAngle INPUT_WRIST_ROTATION_RATE = new NTAngle("Input/Arm/Nudge Wrist Rate", 90, NTAngle.Mode.CCW_DEG);

    public static final NTDouble SWERVE_TURN_KP = new NTDouble("Swerve/Modules/Turn kP", 0.2);
    public static final NTDouble SWERVE_TURN_KI = new NTDouble("Swerve/Modules/Turn kI", 0.0);
    public static final NTDouble SWERVE_TURN_KD = new NTDouble("Swerve/Modules/Turn kD", 0.1);

    public static final NTAngle SWERVE_OFFSET_FL = new NTAngle("Swerve/Modules/Front Left/Offset Degrees", 0, NTAngle.Mode.CCW_DEG);
    public static final NTAngle SWERVE_OFFSET_FR = new NTAngle("Swerve/Modules/Front Right/Offset Degrees", 0, NTAngle.Mode.CCW_DEG);
    public static final NTAngle SWERVE_OFFSET_BL = new NTAngle("Swerve/Modules/Back Left/Offset Degrees", 0, NTAngle.Mode.CCW_DEG);
    public static final NTAngle SWERVE_OFFSET_BR = new NTAngle("Swerve/Modules/Back Right/Offset Degrees", 0, NTAngle.Mode.CCW_DEG);

    public static final NTDouble ARM_MOVE_KP = new NTDouble("Arm/Move PID/kP", 8);
    public static final NTDouble ARM_MOVE_KI = new NTDouble("Arm/Move PID/kI", 0);
    public static final NTDouble ARM_MOVE_KD = new NTDouble("Arm/Move PID/kD", 0);
    public static final NTDouble ARM_WRIST_KP = new NTDouble("Arm/Wrist PID/kP", 0.1);
    public static final NTDouble ARM_WRIST_KI = new NTDouble("Arm/Wrist PID/kI", 0);
    public static final NTDouble ARM_WRIST_KD = new NTDouble("Arm/Wrist PID/kD", 0);

    public static final NTDouble ARM_MAX_SPEED = new NTDouble("Arm/Max Speed", 1.0);
    public static final NTDouble ARM_STOP_TOL = new NTDouble("Arm/Stop Tolerance", 1.5);
    public static final NTDouble ARM_START_TOL = new NTDouble("Arm/Start Tolerance", 2.5);

    public static final NTAngle ARM_BOTTOM_OFFSET = new NTAngle("Arm/Offsets/Bottom", 0, NTAngle.Mode.CCW_DEG);
    public static final NTAngle ARM_TOP_OFFSET = new NTAngle("Arm/Offsets/Top", 0, NTAngle.Mode.CCW_DEG);
    public static final NTAngle ARM_WRIST_OFFSET = new NTAngle("Arm/Offsets/Wrist", 0, NTAngle.Mode.CCW_DEG);

    public static final NTVec2d ARM_FOLD_ZONE = new NTVec2d("Arm/Fold Zone", 0.5, 0.25);
    public static final NTAngle ARM_FOLD_ANGLE_CUBE = new NTAngle("Arm/Fold Angle/Cube", 0, NTAngle.Mode.CCW_DEG);
    public static final NTAngle ARM_FOLD_ANGLE_CONE = new NTAngle("Arm/Fold Angle/Cone", 0, NTAngle.Mode.CCW_DEG);

    public static final NTDouble ARM_WRIST_FULL_HOLD = new NTDouble("Arm/Wrist Full Hold Pct", 0.09);

    public static final NTDouble INTAKE_CONE_IN_SPEED = new NTDouble("Intake/Cone Intake Speed", 0);
    public static final NTDouble INTAKE_CONE_OUT_SPEED = new NTDouble("Intake/Cone Outtake Speed", 0);
    public static final NTDouble INTAKE_CONE_HOLD = new NTDouble("Intake/Cone Hold", 0);

    public static final NTDouble INTAKE_CUBE_IN_SPEED = new NTDouble("Intake/Cube Intake Speed", 0);
    public static final NTDouble INTAKE_CUBE_OUT_SPEED = new NTDouble("Intake/Cube Outtake Speed", 0);
    public static final NTDouble INTAKE_CUBE_HOLD = new NTDouble("Intake/Cube Hold", 0);
}
