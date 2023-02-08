package com.swrobotics.pathfinding.arm;

import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.pathfinding.core.grid.BitfieldGrid;
import com.swrobotics.pathfinding.core.grid.Point;
import com.swrobotics.shared.arm.ArmPose;

public final class ArmStateSpaceGrid extends BitfieldGrid {
    private static final int RESOLUTION = 64;

    private static final double MIN_BOTTOM_ANGLE = 0;
    private static final double MAX_BOTTOM_ANGLE = Math.PI;
    private static final double MIN_TOP_ANGLE = 0;
    private static final double MAX_TOP_ANGLE = 2 * Math.PI;

    public static ArmPose pointToPose(Point point) {
        return positionToPose(point.x, point.y);
    }

    private static ArmPose positionToPose(double botIdx, double topIdx) {
        return new ArmPose(
                MathUtil.lerp(MIN_BOTTOM_ANGLE, MAX_BOTTOM_ANGLE, botIdx / RESOLUTION),
                MathUtil.wrap(MathUtil.lerp(MIN_TOP_ANGLE, MAX_TOP_ANGLE, topIdx / RESOLUTION) + Math.PI, 0, Math.PI * 2)
        );
    }

    private static ArmPose wrap(ArmPose pose) {
        double wrapBot = MathUtil.wrap(pose.bottomAngle, 0, Math.PI * 2);
        double wrapTop = MathUtil.wrap(pose.topAngle, 0, Math.PI * 2);
        return new ArmPose(wrapBot, wrapTop);
    }

    public static Point closestPoint(ArmPose pose) {
        pose = wrap(pose);
        double bot = MathUtil.map(pose.bottomAngle, MIN_BOTTOM_ANGLE, MAX_BOTTOM_ANGLE, 0, RESOLUTION);
        double top = MathUtil.map(MathUtil.wrap(pose.topAngle + Math.PI, 0, Math.PI * 2), MIN_TOP_ANGLE, MAX_TOP_ANGLE, 0, RESOLUTION);

        int botIdx = (int) Math.round(bot);
        if (botIdx < 0) botIdx = 0;
        if (botIdx >= RESOLUTION) botIdx = RESOLUTION;

        int topIdx = (int) Math.round(top);
        if (topIdx < 0) topIdx = 0;
        if (topIdx >= RESOLUTION) topIdx = RESOLUTION;

        return new Point(botIdx, topIdx);
    }

    public ArmStateSpaceGrid() {
        super(RESOLUTION, RESOLUTION);

        for (int top = 0; top < RESOLUTION; top++) {
            for (int bot = 0; bot < RESOLUTION; bot++) {
                ArmPose pose = positionToPose(bot + 0.5, top + 0.5);
                set(bot, top, pose.isValid());
            }
        }
    }
}
