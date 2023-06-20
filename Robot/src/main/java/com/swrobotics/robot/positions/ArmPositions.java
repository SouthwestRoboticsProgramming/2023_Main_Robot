package com.swrobotics.robot.positions;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.robot.subsystems.arm.ArmPosition;

// FIXME: Monch has different arm positions
public final class ArmPositions {
    public static final ArmPosition.NT CUBE_UPPER =
            new ArmPosition.NT(
                    "Arm/Positions/Cube High", 1.4318269885638122, 0.8806285764310378, Angle.ZERO);
    public static final ArmPosition.NT CUBE_CENTER =
            new ArmPosition.NT("Arm/Positions/Cube Mid", 1.097013517546996, 0.48231282959042676, Angle.ZERO);
    public static final ArmPosition.NT CONE_UPPER =
            new ArmPosition.NT("Arm/Positions/Cone High", 1.3644986059129367, 0.9724653611111114, Angle.ZERO);
    public static final ArmPosition.NT CONE_CENTER =
            new ArmPosition.NT("Arm/Positions/Cone Mid", 0.9729467159847158, 0.7475451032941605, Angle.ZERO);

    public static final ArmPosition.NT CONE_PICKUP =
            new ArmPosition.NT("Arm/Positions/Cone Pickup", 1.389016915417349, 0.8877022517101628, Angle.ZERO);
    public static final ArmPosition.NT CUBE_PICKUP =
            new ArmPosition.NT("Arm/Positions/Cube Pickup", 1.1902065890352667, 0.846324874042444, Angle.ZERO);

    public static final ArmPosition.NT DEFAULT =
            new ArmPosition.NT("Arm/Positions/Default", 0.789397, 0.18905, Angle.ZERO);

    private ArmPositions() {
        throw new AssertionError();
    }
}
