package com.swrobotics.lib.drive.swerve;

import com.swrobotics.mathlib.MathUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public enum StopPosition {
    NONE {
        @Override
        public Rotation2d getForModule(Translation2d pos) {
            return new Rotation2d(0);
        }
    },
    CROSS {
        @Override
        public Rotation2d getForModule(Translation2d pos) {
            return pos.getAngle();
        }
    },
    CIRCLE {
        @Override
        public Rotation2d getForModule(Translation2d pos) {
            return Rotation2d.fromDegrees(MathUtil.wrap(pos.getAngle().getDegrees() + 90, 0, 360));
        }
    };

    /**
     * Gets the target rotation for a module at a given position
     *
     * @param pos module position
     * @return module target rotation
     */
    public abstract Rotation2d getForModule(Translation2d pos);
}
