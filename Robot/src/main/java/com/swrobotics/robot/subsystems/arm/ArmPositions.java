package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.mathlib.Angle;

public final class ArmPositions {
    // TODO: All of these need proper defaults
    // TODO: We can maybe calculate flip intake based on the pose
    public static final PositionInfo DEFAULT =
            new PositionInfo("Arm/Positions/Default", 0, 0, Angle.ZERO, false);
    public static final PositionSet CONE =
            new PositionSet(
                    new PositionInfo(
                            "Arm/Positions/Cone/Front/Score High", 0, 0, Angle.ZERO, false),
                    new FrontBackPair(
                            new PositionInfo(
                                    "Arm/Positions/Cone/Front/Score Mid", 0, 0, Angle.ZERO, false),
                            new PositionInfo(
                                    "Arm/Positions/Cone/Back/Score Mid", 0, 0, Angle.ZERO, false)),
                    new FrontBackPair(
                            new PositionInfo(
                                    "Arm/Positions/Cone/Front/Floor Pickup",
                                    0,
                                    0,
                                    Angle.ZERO,
                                    false),
                            new PositionInfo(
                                    "Arm/Positions/Cone/Back/Floor Pickup",
                                    0,
                                    0,
                                    Angle.ZERO,
                                    false)),
                    new FrontBackPair(
                            new PositionInfo(
                                    "Arm/Positions/Cone/Front/Chute Pickup",
                                    0,
                                    0,
                                    Angle.ZERO,
                                    false),
                            new PositionInfo(
                                    "Arm/Positions/Cone/Back/Chute Pickup",
                                    0,
                                    0,
                                    Angle.ZERO,
                                    false)),
                    new FrontBackPair(
                            new PositionInfo(
                                    "Arm/Positions/Cone/Front/Substation Pickup",
                                    0,
                                    0,
                                    Angle.ZERO,
                                    false),
                            new PositionInfo(
                                    "Arm/Positions/Cone/Back/Substation Pickup",
                                    0,
                                    0,
                                    Angle.ZERO,
                                    false)));
    public static final PositionSet CUBE =
            new PositionSet(
                    new PositionInfo(
                            "Arm/Positions/Cube/Front/Score High", 0, 0, Angle.ZERO, false),
                    new FrontBackPair(
                            new PositionInfo(
                                    "Arm/Positions/Cube/Front/Score Mid", 0, 0, Angle.ZERO, false),
                            new PositionInfo(
                                    "Arm/Positions/Cube/Back/Score Mid", 0, 0, Angle.ZERO, false)),
                    new FrontBackPair(
                            new PositionInfo(
                                    "Arm/Positions/Cube/Front/Floor Pickup",
                                    0,
                                    0,
                                    Angle.ZERO,
                                    false),
                            new PositionInfo(
                                    "Arm/Positions/Cube/Back/Floor Pickup",
                                    0,
                                    0,
                                    Angle.ZERO,
                                    false)),
                    new FrontBackPair(
                            new PositionInfo(
                                    "Arm/Positions/Cube/Front/Chute Pickup",
                                    0,
                                    0,
                                    Angle.ZERO,
                                    false),
                            new PositionInfo(
                                    "Arm/Positions/Cube/Back/Chute Pickup",
                                    0,
                                    0,
                                    Angle.ZERO,
                                    false)),
                    new FrontBackPair(
                            new PositionInfo(
                                    "Arm/Positions/Cube/Front/Substation Pickup",
                                    0,
                                    0,
                                    Angle.ZERO,
                                    false),
                            new PositionInfo(
                                    "Arm/Positions/Cube/Back/Substation Pickup",
                                    0,
                                    0,
                                    Angle.ZERO,
                                    false)));
    public static final FrontBackPair DOWNED_CONE_FLOOR_PICKUP =
            new FrontBackPair(
                    new PositionInfo(
                            "Arm/Positions/Downed Cone/Front/Floor Pickup",
                            0,
                            0,
                            Angle.ZERO,
                            false),
                    new PositionInfo(
                            "Arm/Positions/Downed Cone/Back/Floor Pickup",
                            0,
                            0,
                            Angle.ZERO,
                            false));

    public static final class PositionInfo {
        public final NTBoolean flipIntake;
        public final ArmPosition.NT position;

        public PositionInfo(
                String path, double defX, double defY, Angle defWrist, boolean defFlipIntake) {
            flipIntake = new NTBoolean(path + "/Flip Intake", defFlipIntake);
            position = new ArmPosition.NT(path + "/Position", defX, defY, defWrist);
        }
    }

    public static final class FrontBackPair {
        public final PositionInfo front;
        public final PositionInfo back;

        public FrontBackPair(PositionInfo front, PositionInfo back) {
            this.front = front;
            this.back = back;
        }
    }

    public static final class PositionSet {
        public final PositionInfo scoreHighFront;
        public final FrontBackPair scoreMid;
        public final FrontBackPair floorPickup;
        public final FrontBackPair chutePickup;
        public final FrontBackPair substationPickup;

        public PositionSet(
                PositionInfo scoreHighFront,
                FrontBackPair scoreMid,
                FrontBackPair floorPickup,
                FrontBackPair chutePickup,
                FrontBackPair substationPickup) {
            this.scoreHighFront = scoreHighFront;
            this.scoreMid = scoreMid;
            this.floorPickup = floorPickup;
            this.chutePickup = chutePickup;
            this.substationPickup = substationPickup;
        }
    }
}
