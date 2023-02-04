package com.swrobotics.robot.subsystems;

import com.swrobotics.robot.input.ButtonPanel;
import com.swrobotics.robot.positions.MatrixDefs;
import com.swrobotics.robot.positions.MatrixPos;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class MatrixPosHandlerSubsystem extends SubsystemBase {
    public ButtonPanel panel;
    public MatrixDefs matrixDefs;


    public MatrixPosHandlerSubsystem(ButtonPanel panel) {
        this.panel = panel;
        matrixDefs = new MatrixDefs();
    }

    @Override
    public void periodic() {
        int[] scoringTarget = panel.getScoringButtons();
        if (scoringTarget == new int[]{-1, -1}) {
            return;
        }

        pathfindtopoint(matrixDefs.getMatrixPos(scoringTarget[0], scoringTarget[1]));
    }
    public void pathfindtopoint(MatrixPos pos) {

    }
}
