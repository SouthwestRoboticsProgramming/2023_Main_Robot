package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;

public final class ArmPositions {
    // TODO: All of these need proper defaults
    public static final ArmPosition.NT DEFAULT =
            new ArmPosition.NT("Arm/Positions/Default", new ArmPose(CCWAngle.deg(90), CCWAngle.deg(-90), Angle.ZERO).toPosition());
    public static final PositionSet CONE =
            new PositionSet(
                    new ArmPosition.NT(
                            "Arm/Positions/Cone/Front/Score High", 0, 0, Angle.ZERO),
                    new FrontBackPair(
                            new ArmPosition.NT(
                                    "Arm/Positions/Cone/Front/Score Mid", 0, 0, Angle.ZERO),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cone/Back/Score Mid", 0, 0, Angle.ZERO)),
                    new FrontBackPair(
                            new ArmPosition.NT(
                                    "Arm/Positions/Cone/Front/Floor Pickup",
                                    0,
                                    0,
                                    Angle.ZERO),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cone/Back/Floor Pickup",
                                    0,
                                    0,
                                    Angle.ZERO)),
                    new FrontBackPair(
                            new ArmPosition.NT(
                                    "Arm/Positions/Cone/Front/Chute Pickup",
                                    0,
                                    0,
                                    Angle.ZERO),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cone/Back/Chute Pickup",
                                    0,
                                    0,
                                    Angle.ZERO)),
                    new FrontBackPair(
                            new ArmPosition.NT(
                                    "Arm/Positions/Cone/Front/Substation Pickup",
                                    0,
                                    0,
                                    Angle.ZERO),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cone/Back/Substation Pickup",
                                    0,
                                    0,
                                    Angle.ZERO)));
    public static final PositionSet CUBE =
            new PositionSet(
                    new ArmPosition.NT(
                            "Arm/Positions/Cube/Front/Score High", 0, 0, Angle.ZERO),
                    new FrontBackPair(
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Front/Score Mid", 0, 0, Angle.ZERO),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Back/Score Mid", 0, 0, Angle.ZERO)),
                    new FrontBackPair(
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Front/Floor Pickup",
                                    0,
                                    0,
                                    Angle.ZERO),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Back/Floor Pickup",
                                    0,
                                    0,
                                    Angle.ZERO)),
                    new FrontBackPair(
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Front/Chute Pickup",
                                    0,
                                    0,
                                    Angle.ZERO),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Back/Chute Pickup",
                                    0,
                                    0,
                                    Angle.ZERO)),
                    new FrontBackPair(
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Front/Substation Pickup",
                                    0,
                                    0,
                                    Angle.ZERO),
                            new ArmPosition.NT(
                                    "Arm/Positions/Cube/Back/Substation Pickup",
                                    0,
                                    0,
                                    Angle.ZERO)));
    public static final FrontBackPair DOWNED_CONE_FLOOR_PICKUP =
            new FrontBackPair(
                    new ArmPosition.NT(
                            "Arm/Positions/Downed Cone/Front/Floor Pickup",
                            0,
                            0,
                            Angle.ZERO),
                    new ArmPosition.NT(
                            "Arm/Positions/Downed Cone/Back/Floor Pickup",
                            0,
                            0,
                            Angle.ZERO));

    public static final class FrontBackPair {
        public final ArmPosition.NT front;
        public final ArmPosition.NT back;

        public FrontBackPair(ArmPosition.NT front, ArmPosition.NT back) {
            this.front = front;
            this.back = back;
        }
    }

    public static final class PositionSet {
        public final ArmPosition.NT scoreHighFront;
        public final FrontBackPair scoreMid;
        public final FrontBackPair floorPickup;
        public final FrontBackPair chutePickup;
        public final FrontBackPair substationPickup;

        public PositionSet(
                ArmPosition.NT scoreHighFront,
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
