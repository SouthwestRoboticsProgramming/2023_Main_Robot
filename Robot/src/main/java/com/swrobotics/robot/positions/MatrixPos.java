package com.swrobotics.robot.positions;

import com.swrobotics.mathlib.Vec2d;
import edu.wpi.first.math.geometry.Translation2d;

public class MatrixPos {
    public Vec2d stopPosition;
    public Translation2d armPosition;

    public int x = 0;
    public int y = 0;

    public MatrixPos(int x, int y, Translation2d stopPosition, Translation2d armPosition) {
        this.stopPosition = stopPosition;
        this.armPosition = armPosition;
        this.x = x;
        this.y = y;
    }
}
