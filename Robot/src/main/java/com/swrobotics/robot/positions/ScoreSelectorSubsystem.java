package com.swrobotics.robot.positions;

import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.input.ButtonPanel;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public final class ScoreSelectorSubsystem extends SubsystemBase {
    private final RobotContainer robot;
    private final ButtonPanel panel;

    private Command currentScoreCommand;

    public ScoreSelectorSubsystem(RobotContainer robot) {
        this.robot = robot;
        panel = robot.buttonPanel;
        currentScoreCommand = null;
    }

    @Override
    public void periodic() {
        for (int x = 0; x < ButtonPanel.WIDTH; x++) {
            for (int y = 0; y < 3; y++) {
                panel.setLightOn(x, y, panel.isButtonDown(x, y));
                if (panel.isButtonRising(x, y)) {
                    if (currentScoreCommand != null) {
                        System.out.println("Cancelled current score sequence");
                        currentScoreCommand.cancel();
                    }

                    System.out.println("Started score sequence towards " + x + ", " + y);
                    currentScoreCommand = ScoringPositions.moveToPosition(robot, x, y);
                    currentScoreCommand.schedule();
                }
            }
        }
    }
}
