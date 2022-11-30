package com.swrobotics.robot.control;

import java.util.function.Supplier;

import com.swrobotics.mathlib.Vec2d;

public interface Input {
    Supplier<Vec2d> getDriveTranslation();

    Supplier<Double> getDriveRotation();
}
