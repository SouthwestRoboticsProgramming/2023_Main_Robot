package com.swrobotics.robot.subsystems.arm;

import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;

public final class ArmVisualizer {
  private final MechanismLigament2d bottom, top;

  public ArmVisualizer(
      double x,
      double y,
      Mechanism2d visualization,
      String name,
      Color bottomColor,
      Color topColor) {
    MechanismRoot2d root = visualization.getRoot(name, x, y);
    bottom =
        root.append(
            new MechanismLigament2d(
                "Bottom Arm", ArmConstants.BOTTOM_LENGTH, 0, 6, new Color8Bit(bottomColor)));
    top =
        bottom.append(
            new MechanismLigament2d(
                "Top Arm", ArmConstants.TOP_LENGTH, 0, 6, new Color8Bit(topColor)));
  }

  public void setPose(ArmPose pose) {
    bottom.setAngle(Math.toDegrees(pose.bottomAngle));

    // Visualization is relative, pose is absolute
    top.setAngle(Math.toDegrees(pose.topAngle - pose.bottomAngle));
  }
}
