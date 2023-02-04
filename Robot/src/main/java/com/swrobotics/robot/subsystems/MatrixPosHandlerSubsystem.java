package com.swrobotics.robot.subsystems;

import com.swrobotics.lib.swerve.commands.PathfindToPointCommand;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.input.ButtonPanel;
import com.swrobotics.robot.positions.MatrixDefs;
import com.swrobotics.robot.positions.MatrixPos;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.Arrays;

public class MatrixPosHandlerSubsystem extends SubsystemBase {
    public ButtonPanel panel;
    public MatrixDefs matrixDefs;

    public RobotContainer container;


    public MatrixPosHandlerSubsystem(ButtonPanel panel, RobotContainer container) {
        this.panel = panel;
        matrixDefs = new MatrixDefs();
        this.container = container;
    }

    @Override
    public void periodic() {
        int[] scoringTarget = panel.getScoringButtons();
        if (Arrays.equals(scoringTarget, new int[]{-1, -1})) {
            return;
        }

        pathfindtopoint(scoringTarget[0], scoringTarget[1]);
    }
    public void pathfindtopoint(int x, int y) {
        MatrixPos pos = matrixDefs.getMatrixPos(x, y);
        PathfindToPointCommand driveCommand = new PathfindToPointCommand(container, pos.stopPosition);

        panel.setLightOn(x,y, true);

        driveCommand.schedule();
    }
}
