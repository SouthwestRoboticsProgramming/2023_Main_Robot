package com.swrobotics.lib.field;

import com.swrobotics.mathlib.MathUtil;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;

/** Represents which form of symmetry the field layout uses. */
public enum FieldSymmetry {
    /**
     * Lateral alliance symmetry. This represents symmetry as if there was a mirror along the center
     * line of the field. With lateral symmetry, the two sides of the field are flipped relative
     * from the driver station's point of view. An example of this is Charged Up from 2023.
     */
    LATERAL {
        @Override
        protected Pose2d flipRed(Pose2d bluePose, FieldInfo field) {
            return new Pose2d(
                    new Translation2d(field.getWidth() - bluePose.getX(), bluePose.getY()),
                    new Rotation2d(
                            MathUtil.wrap(
                                    Math.PI - bluePose.getRotation().getRadians(),
                                    0,
                                    2 * Math.PI)));
        }
    },

    /**
     * Rotational alliance symmetry. This represents symmetry as if the field was rotated 180
     * degrees around its center. With rotational symmetry, both sides of the field are equivalent
     * from the driver station's point of view. Some examples of this are Rapid React from 2022 and
     * Infinite Recharge from 2020.
     */
    ROTATIONAL {
        @Override
        protected Pose2d flipRed(Pose2d bluePose, FieldInfo field) {
            return new Pose2d(
                    field.getSize().translation2d().minus(bluePose.getTranslation()),
                    new Rotation2d(
                            MathUtil.wrap(
                                    bluePose.getRotation().getRadians() + Math.PI,
                                    0,
                                    2 * Math.PI)));
        }
    };

    /**
     * Flips a pose relative to the blue alliance side to be relative to the red alliance side.
     *
     * @param bluePose pose relative to blue alliance
     * @param field information about the field
     * @return pose relative to red alliance
     */
    protected abstract Pose2d flipRed(Pose2d bluePose, FieldInfo field);

    /**
     * Flips a pose to be relative to the current alliance.
     *
     * @param bluePose pose as if the robot was on blue alliance
     * @return pose relative to the current alliance
     */
    public Pose2d flipForAlliance(Pose2d bluePose, FieldInfo field) {
        if (DriverStation.getAlliance() == DriverStation.Alliance.Blue) {
            return bluePose;
        }

        return flipRed(bluePose, field);
    }
}
