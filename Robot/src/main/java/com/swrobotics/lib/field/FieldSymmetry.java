package com.swrobotics.lib.field;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * Represents which form of symmetry the field layout uses.
 */
public enum FieldSymmetry {
    /**
     * Lateral alliance symmetry. This represents symmetry as if there
     * was a mirror along the center line of the field. With lateral
     * symmetry, the two sides of the field are flipped relative from
     * the driver station's point of view. An example of this is
     * Charged Up from 2023.
     */
    LATERAL {
        @Override
        protected Pose2d flipRed(Pose2d bluePose) {
            return null;
        }
    },

    /**
     * Rotational alliance symmetry. This represents symmetry as if the
     * field was rotated 180 degrees around its center. With rotational
     * symmetry, both sides of the field are equivalent from the driver
     * station's point of view. Some examples of this are Rapid React
     * from 2022 and Infinite Recharge from 2020.
     */
    ROTATIONAL {
        @Override
        protected Pose2d flipRed(Pose2d bluePose) {
            return null;
        }
    };

    protected abstract Pose2d flipRed(Pose2d bluePose);

    /**
     * Flips a pose to be relative to the current alliance.
     *
     * @param bluePose pose as if the robot was on blue alliance
     * @return pose relative to the current alliance
     */
    public Pose2d flipForAlliance(Pose2d bluePose) {
        if (DriverStation.getAlliance() == DriverStation.Alliance.Blue) {
            return bluePose;
        }

        return flipRed(bluePose);
    }
}
