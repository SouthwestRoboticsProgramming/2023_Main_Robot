package com.swrobotics.shared.arm;

public final class ArmConstants {
    // Lengths of the arm segments in meters  FIXME
    public static final double BOTTOM_LENGTH = 1.25;
    public static final double TOP_LENGTH = 1;

    // Gear ratios from motor output to arm joint movement
    public static final double BOTTOM_GEAR_RATIO = 600;
    public static final double TOP_GEAR_RATIO = 288;

    private ArmConstants() {
        throw new AssertionError();
    }
}
