package com.swrobotics.robot.control;

import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.commands.DefaultDriveCommand;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.Button;

public class InputSelector extends SubsystemBase {

    private final RobotContainer robot;
    private final SendableChooser<Runnable> chooser;
    private Input input;

    private Runnable lastOption;

    public InputSelector(RobotContainer robot) {
        this.robot = robot;

        // Create a chooser that allows selection of inputs through ShuffleBoard
        chooser = new SendableChooser<Runnable>();

        SmartDashboard.putData("Input chooser", chooser);

        // Add your options here
        chooser.setDefaultOption("Xbox Controller", this::setXbox);
        chooser.addOption("No input", this::setNull);

        lastOption = chooser.getSelected(); // Set what was chosen to see if it has changed
        updateInput();
    }

    @Override
    public void periodic() {
        // Check if the chosen option has changed
        if (lastOption == chooser.getSelected()) {
            lastOption = chooser.getSelected();
            return;
        }

        // If is has changed, update the input type
        updateInput();
    }

    private void updateInput() {
        chooser.getSelected().run();
        configureInput();
    }

    private void configureInput() {
        // Configure drive
        robot.m_drivetrainSubsystem.setDefaultCommand(new DefaultDriveCommand(
            robot.m_drivetrainSubsystem,
            () -> input.getDriveX() * DrivetrainSubsystem.MAX_ACHIEVABLE_VELOCITY_METERS_PER_SECOND,
            () -> input.getDriveY() * DrivetrainSubsystem.MAX_ACHIEVABLE_VELOCITY_METERS_PER_SECOND,
            () -> input.getDriveRotation()
                    * DrivetrainSubsystem.MAX_ANGULAR_VELOCITY_RADIANS_PER_SECOND));

        // Configure gyro reset
        input.getResetGyroButton()
            // No requirements because we don't need to interrupt anything
            .whenPressed(robot.m_drivetrainSubsystem::zeroGyroscope);
    }


    private void setXbox() {
        input = new XboxInput();
    }

    private void setNull() {
        input = new Input() {

            @Override
            public double getDriveX() { return 0; }

            @Override
            public double getDriveY() { return 0; }

            @Override
            public double getDriveRotation() { return 0; }

            @Override
            public Button getResetGyroButton() { return new Button(); }
            
        };
    }
    
}
