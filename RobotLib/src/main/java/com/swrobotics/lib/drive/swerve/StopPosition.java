package com.swrobotics.lib.drive.swerve;

import com.swrobotics.mathlib.MathUtil;

import edu.wpi.first.math.geometry.Rotation2d;

/** Represents the arrangement the swerve modules default to when not moving. */
public enum StopPosition {
  /**
   * Keeps the modules in the same direction they are facing.
   * Note: This does not affect the brake mode of the modules. That must be set separately.
   */
  COAST {
    @Override
    public Rotation2d getForModule(SwerveModule module) {
      return module.getAngle();
    }
  },

  /** Points all modules forward relative to the robot. */
  FORWARD {
    @Override
    public Rotation2d getForModule(SwerveModule module) {
      return new Rotation2d(0);
    }
  },

  /** Points all modules towards the center of the robot, making it harder to push the robot. */
  CROSS {
    @Override
    public Rotation2d getForModule(SwerveModule module) {
      return module.position.getAngle();
    }
  },

  /**
   * Arranges the modules perpendicular to the center of the robot, allowing for quicker rotation.
   */
  CIRCLE {
    @Override
    public Rotation2d getForModule(SwerveModule module) {
      return Rotation2d.fromDegrees(
          MathUtil.wrap(module.position.getAngle().getDegrees() + 90, 0, 360));
    }
  };

  /**
   * Gets the target rotation for a module at a given position
   *
   * @param module module position
   * @return module target rotation
   */
  public abstract Rotation2d getForModule(SwerveModule module);
}
