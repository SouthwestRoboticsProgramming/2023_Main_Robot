package com.swrobotics.shared.arm;

import edu.wpi.first.math.util.Units;

public final class ArmConstants {
    // Lengths of the arm segments in meters
    public static final double BOTTOM_LENGTH = Units.inchesToMeters(38.5);
    public static final double TOP_LENGTH = Units.inchesToMeters(49.2559230148);
    public static final double WRIST_LENGTH = Units.inchesToMeters(12.0); // FIXME

    // Gear ratios from motor output to arm joint movement
    public static final double BOTTOM_GEAR_RATIO = 600;
    public static final double TOP_GEAR_RATIO = 288;
    public static final double WRIST_GEAR_RATIO = 100; // FIXME

    private ArmConstants() {
        throw new AssertionError();
    }
}
