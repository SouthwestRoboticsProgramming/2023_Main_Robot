package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.shared.arm.ArmConstants;
import com.swrobotics.shared.arm.ArmPose;

import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;

public final class ArmVisualizer {
    private final MechanismLigament2d bottom, top, wrist;

    public ArmVisualizer(
            double x,
            double y,
            Mechanism2d visualization,
            String name,
            Color bottomColor,
            Color topColor,
            Color wristColor) {
        MechanismRoot2d root = visualization.getRoot(name, x, y);
        bottom = root.append(
                new MechanismLigament2d(
                        "Bottom Arm",
                        ArmConstants.BOTTOM_LENGTH,
                        0,
                        6,
                        new Color8Bit(bottomColor)));
        top = bottom.append(
                new MechanismLigament2d(
                        "Top Arm", ArmConstants.TOP_LENGTH, 0, 6, new Color8Bit(topColor)));
        wrist = top.append(
                new MechanismLigament2d("Wrist", ArmConstants.WRIST_LENGTH / 2, 70, 6, new Color8Bit(wristColor)));
    }

    public void setPose(ArmPose pose) {
        bottom.setAngle(Math.toDegrees(pose.bottomAngle));

        // Visualization is relative, pose is absolute
        top.setAngle(Math.toDegrees(pose.topAngle - pose.bottomAngle));

        wrist.setAngle(pose.wristAngle);
    }
}
