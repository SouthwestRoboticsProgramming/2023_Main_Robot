package com.swrobotics.robot.positions;

import com.swrobotics.lib.net.NTTranslation2d;

public final class ArmPositions {
    public static final NTTranslation2d CUBE_UPPER =
            new NTTranslation2d("Arm/Positions/Cube High", 1.4318269885638122, 0.8806285764310378);
    public static final NTTranslation2d CUBE_CENTER =
            new NTTranslation2d("Arm/Positions/Cube Mid", 1.097013517546996, 0.48231282959042676);
    public static final NTTranslation2d CONE_UPPER =
            new NTTranslation2d("Arm/Positions/Cone High", 1.3644986059129367, 0.9724653611111114);
    public static final NTTranslation2d CONE_CENTER =
            new NTTranslation2d("Arm/Positions/Cone Mid", 0.9729467159847158, 0.7475451032941605);

    public static final NTTranslation2d CONE_PICKUP =
            new NTTranslation2d("Arm/Positions/Cone Pickup", 1.389016915417349, 0.8877022517101628);
    public static final NTTranslation2d CUBE_PICKUP =
            new NTTranslation2d("Arm/Positions/Cube Pickup", 1.1902065890352667, 0.846324874042444);

    public static final NTTranslation2d DEFAULT =
            new NTTranslation2d("Arm/Positions/Default", 0.789397, 0.18905);

    private ArmPositions() {
        throw new AssertionError();
    }
}
