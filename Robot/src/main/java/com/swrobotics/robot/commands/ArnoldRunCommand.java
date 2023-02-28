package com.swrobotics.robot.commands;

import com.swrobotics.robot.input.ButtonPanel;
import com.swrobotics.robot.subsystems.arnold.Arnold;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class ArnoldRunCommand extends CommandBase {
    private final Arnold arnold;
    public final ButtonPanel buttonPanel;

    public ArnoldRunCommand(Arnold arnold, ButtonPanel buttonPanel) {
        this.arnold = arnold;
        this.buttonPanel = buttonPanel;
        addRequirements(arnold);
    }

    @Override
    public void execute() {
        boolean isSpinning = arnold.isSpinning();
        buttonPanel.setLightOn(7, 3, isSpinning);
        if (!isSpinning) {
            arnold.setState(Arnold.ArnoldState.IN);
        } else {
            arnold.setState(Arnold.ArnoldState.STOP);
        }

    }

    @Override
    public boolean isFinished() {
        return true;
    }


}
