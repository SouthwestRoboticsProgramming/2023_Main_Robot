package com.swrobotics.robot.config;

import com.swrobotics.lib.net.*;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.Vec2d;

/**
 * Class to store all tunable NetworkTables values in one place, to make it
 * easier to hardcode the defaults in case of data loss.
 *
 * Temporary values should not be here, this class is only for persistent data.
 */
// TODO: Set all the defaults
public final class NTData {
    public static final NTEntry<Double> INPUT_SPEED_RATE_LIMIT = new NTDouble("Input/Speed Slew Limit", 20).setPersistent();
    public static final NTEntry<Double> INPUT_ARM_TRANSLATION_RATE = new NTDouble("Input/Arm/Nudge Translation Rate", 1).setPersistent();
    public static final NTEntry<Angle> INPUT_WRIST_ROTATION_RATE = new NTAngle("Input/Arm/Nudge Wrist Rate", 90, NTAngle.Mode.CCW_DEG).setPersistent();

    public static final NTEntry<Double> SWERVE_TURN_KP = new NTDouble("Swerve/Modules/Turn kP", 0.2).setPersistent();
    public static final NTEntry<Double> SWERVE_TURN_KI = new NTDouble("Swerve/Modules/Turn kI", 0.0).setPersistent();
    public static final NTEntry<Double> SWERVE_TURN_KD = new NTDouble("Swerve/Modules/Turn kD", 0.1).setPersistent();

    public static final NTEntry<Angle> SWERVE_OFFSET_FL = new NTAngle("Swerve/Modules/Front Left/Offset Degrees", 0, NTAngle.Mode.CCW_DEG).setPersistent();
    public static final NTEntry<Angle> SWERVE_OFFSET_FR = new NTAngle("Swerve/Modules/Front Right/Offset Degrees", 0, NTAngle.Mode.CCW_DEG).setPersistent();
    public static final NTEntry<Angle> SWERVE_OFFSET_BL = new NTAngle("Swerve/Modules/Back Left/Offset Degrees", 0, NTAngle.Mode.CCW_DEG).setPersistent();
    public static final NTEntry<Angle> SWERVE_OFFSET_BR = new NTAngle("Swerve/Modules/Back Right/Offset Degrees", 0, NTAngle.Mode.CCW_DEG).setPersistent();

    public static final NTEntry<Double> ARM_MOVE_KP = new NTDouble("Arm/Move PID/kP", 8).setPersistent();
    public static final NTEntry<Double> ARM_MOVE_KI = new NTDouble("Arm/Move PID/kI", 0).setPersistent();
    public static final NTEntry<Double> ARM_MOVE_KD = new NTDouble("Arm/Move PID/kD", 0).setPersistent();
    public static final NTEntry<Double> ARM_WRIST_KP = new NTDouble("Arm/Wrist PID/kP", 0.1).setPersistent();
    public static final NTEntry<Double> ARM_WRIST_KI = new NTDouble("Arm/Wrist PID/kI", 0).setPersistent();
    public static final NTEntry<Double> ARM_WRIST_KD = new NTDouble("Arm/Wrist PID/kD", 0).setPersistent();

    public static final NTEntry<Double> ARM_MAX_SPEED = new NTDouble("Arm/Max Speed", 1.0).setPersistent();
    public static final NTEntry<Double> ARM_STOP_TOL = new NTDouble("Arm/Stop Tolerance", 1.5).setPersistent();
    public static final NTEntry<Double> ARM_START_TOL = new NTDouble("Arm/Start Tolerance", 2.5).setPersistent();

    public static final NTEntry<Angle> ARM_BOTTOM_OFFSET = new NTAngle("Arm/Offsets/Bottom", 0, NTAngle.Mode.CCW_DEG).setPersistent();
    public static final NTEntry<Angle> ARM_TOP_OFFSET = new NTAngle("Arm/Offsets/Top", 0, NTAngle.Mode.CCW_DEG).setPersistent();
    public static final NTEntry<Angle> ARM_WRIST_OFFSET = new NTAngle("Arm/Offsets/Wrist", 0, NTAngle.Mode.CCW_DEG).setPersistent();

    public static final NTEntry<Vec2d> ARM_FOLD_ZONE = new NTVec2d("Arm/Fold Zone", 0.5, 0.25).setPersistent();
    public static final NTEntry<Angle> ARM_FOLD_ANGLE_CUBE = new NTAngle("Arm/Fold Angle/Cube", 0, NTAngle.Mode.CCW_DEG).setPersistent();
    public static final NTEntry<Angle> ARM_FOLD_ANGLE_CONE = new NTAngle("Arm/Fold Angle/Cone", 0, NTAngle.Mode.CCW_DEG).setPersistent();

    public static final NTEntry<Boolean> ARM_BRAKE_MODE = new NTBoolean("Arm/Brake Mode", true).setPersistent();
    public static final NTEntry<Double> ARM_WRIST_FULL_HOLD = new NTDouble("Arm/Wrist Full Hold Pct", 0.09).setPersistent();

    public static final NTEntry<Double> INTAKE_CONE_IN_SPEED = new NTDouble("Intake/Cone Intake Speed", 0).setPersistent();
    public static final NTEntry<Double> INTAKE_CONE_OUT_SPEED = new NTDouble("Intake/Cone Outtake Speed", 0).setPersistent();
    public static final NTEntry<Double> INTAKE_CONE_HOLD = new NTDouble("Intake/Cone Hold", 0).setPersistent();

    public static final NTEntry<Double> INTAKE_CUBE_IN_SPEED = new NTDouble("Intake/Cube Intake Speed", 0).setPersistent();
    public static final NTEntry<Double> INTAKE_CUBE_OUT_SPEED = new NTDouble("Intake/Cube Outtake Speed", 0).setPersistent();
    public static final NTEntry<Double> INTAKE_CUBE_HOLD = new NTDouble("Intake/Cube Hold", 0).setPersistent();

    public static final NTEntry<Double> BALANCE_STOP_TOL = new NTDouble("Balance/Stop Tolerance", 0).setPersistent(); // FIXME: Tune
    public static final NTEntry<Double> BALANCE_ADJUST_AMT = new NTDouble("Balance/Adjust Amount", 0.385).setPersistent();
    public static final NTEntry<Double> BALANCE_START_END_TOL = new NTDouble("Balance/Start End Tolerance", 0).setPersistent(); // FIXME: Tune
    public static final NTEntry<Double> BALANCE_BLIND_SPEED = new NTDouble("Balance/Blind Speed", -1.5).setPersistent();
}
