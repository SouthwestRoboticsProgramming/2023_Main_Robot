package com.swrobotics.robot.positions;

import com.swrobotics.lib.net.NTTranslation2d;

import edu.wpi.first.math.util.Units;

public final class ArmPositions {
    public static final NTTranslation2d CUBE_UPPER = new NTTranslation2d("Arm/Positions/Cube High", 1.336078, 0.893116);
    public static final NTTranslation2d CUBE_CENTER = new NTTranslation2d("Arm/Positions/Cube Mid", 0.933183, 0.335277);
    public static final NTTranslation2d CONE_UPPER = new NTTranslation2d("Arm/Positions/Cone High", 1.391533, 0.817448);
    public static final NTTranslation2d CONE_CENTER = new NTTranslation2d("Arm/Positions/Cone Mid", 0.869909, 0.460904);

    public static final NTTranslation2d CONE_PICKUP = new NTTranslation2d("Arm/Positions/Cone Pickup", 1.131009, 0.845100 - 0.1);
    public static final NTTranslation2d CUBE_PICKUP = new NTTranslation2d("Arm/Positions/Cube Pickup", 1.120115, 0.600034);

    public static final NTTranslation2d DEFAULT = new NTTranslation2d("Arm/Positions/Default", 0.689397, Units.inchesToMeters(11.5 - 13 + 2.25));

    private ArmPositions() {
        throw new AssertionError();
    }
}
