package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;

// TODO: Merge with NTData?
public final class ArmPositions {
    /*
     *
     */

    // TODO: All of these need proper defaults
    private static final ArmPosition UNSET_DEFAULT = new ArmPose(CCWAngle.deg(90), CCWAngle.deg(-80), Angle.ZERO).toPosition();

    public static final ArmPosition.NT DEFAULT =
            new ArmPosition.NT("Arm/Positions/Default", new ArmPose(CCWAngle.deg(90), CCWAngle.deg(-90), CCWAngle.deg(-90)).toPosition(), false);
    static {
        DEFAULT.setPersistent();
    }

    public static final PositionSet CONE =
            new PositionSet(
                    new ArmPosition.NT(
                            "Arm/Positions/Cone/Back/Score High", -0.906460, 1.234174, CCWAngle.deg(-194.142015), true),
                    new FrontBackPair(
                            null,
                            new ArmPosition.NT(
                                    "Arm/Positions/Cone/Back/Score Mid", -0.619149, 1.150590, CCWAngle.deg(-189.259830), true)),
                    new FrontBackPair(
                            null,
//                            new ArmPosition.NT(
//                                    "Arm/Positions/Cone/Front/Floor Pickup",
//                                    0.797765, 0.263012, CCWAngle.deg(-31.995325), false),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cone/Back/Floor Pickup",
                                    -0.497925, 0.122659, CCWAngle.deg(-143.687955), false)),
                    new FrontBackPair(
                            null, null),
//                            new ArmPosition.NT(
//                                    "Arm/Positions/Cone/Front/Chute Pickup",
//                                    UNSET_DEFAULT, false),
//                            new ArmPosition.NT(
//                                    "Arm/Positions/Cone/Back/Chute Pickup",
//                                    UNSET_DEFAULT, false)),
                    new FrontBackPair(
                            null,
//                            new ArmPosition.NT(
//                                    "Arm/Positions/Cone/Front/Substation Pickup",
//                                    UNSET_DEFAULT, false),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cone/Back/Substation Pickup",
                                    -0.472302, 1.280790, CCWAngle.deg(-186.306754), false)));
    public static final PositionSet CUBE =
            new PositionSet(
                    new ArmPosition.NT(
                            "Arm/Positions/Cube/Front/Score High", 1.133248, 1.048364, CCWAngle.deg(109.709461), true),
                    new FrontBackPair(
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Front/Score Mid", 0.956374, 0.791880, CCWAngle.deg(67.709953), true),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Back/Score Mid", -0.704469, 1.068582, CCWAngle.deg(-52.708538), true)),
                    new FrontBackPair(
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Front/Floor Pickup",
                                    0.599123, 0.003825, CCWAngle.deg(49.571949), false),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Back/Floor Pickup",
                                    -0.780257, 0.089131, CCWAngle.deg(-16.454774), true)),
                    new FrontBackPair(
                            null, null),
//                            new ArmPosition.NT(
//                                    "Arm/Positions/Cube/Front/Chute Pickup",
//                                    UNSET_DEFAULT, false),
//                            new ArmPosition.NT(
//                                    "Arm/Positions/Cube/Back/Chute Pickup",
//                                    UNSET_DEFAULT, false)),
                    new FrontBackPair(
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Front/Substation Pickup",
                                    0.729392, 0.959242, CCWAngle.deg(64.770436), false),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Back/Substation Pickup",
                                    -0.462597, 1.248891, CCWAngle.deg(-61.787313), false)));
    public static final FrontBackPair DOWNED_CONE_FLOOR_PICKUP =
            new FrontBackPair(
                    null, null);
//                    new ArmPosition.NT(
//                            "Arm/Positions/Downed Cone/Front/Floor Pickup",
//                            UNSET_DEFAULT, false),
//                    new ArmPosition.NT(
//                            "Arm/Positions/Downed Cone/Back/Floor Pickup",
//                            UNSET_DEFAULT, false));

    public static final class FrontBackPair {
        public final ArmPosition.NT front;
        public final ArmPosition.NT back;

        public FrontBackPair(ArmPosition.NT front, ArmPosition.NT back) {
            this.front = front;
            this.back = back;

            if (front != null)
                front.setPersistent();
            if (back != null)
                back.setPersistent();
        }
    }

    public static final class PositionSet {
        public final ArmPosition.NT scoreHigh;
        public final FrontBackPair scoreMid;
        public final FrontBackPair floorPickup;
        public final FrontBackPair chutePickup;
        public final FrontBackPair substationPickup;

        public PositionSet(
                ArmPosition.NT scoreHigh,
                FrontBackPair scoreMid,
                FrontBackPair floorPickup,
                FrontBackPair chutePickup,
                FrontBackPair substationPickup) {
            this.scoreHigh = scoreHigh;
            this.scoreMid = scoreMid;
            this.floorPickup = floorPickup;
            this.chutePickup = chutePickup;
            this.substationPickup = substationPickup;

            if (scoreHigh != null)
                scoreHigh.setPersistent();
        }
    }
}
