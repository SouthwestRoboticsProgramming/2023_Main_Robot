package com.swrobotics.robot.positions;

public class MatrixDefs {

    // This is in X and Y X first Y Second (Dont Forget Mason)
    public MatrixPos[][] poses = new MatrixPos[9][3];

    public MatrixDefs() {
        // TODO: Actually Construct working positions
    }

    public MatrixPos getMatrixPos(int x, int y) {
        return poses[x][y];
    }


}
