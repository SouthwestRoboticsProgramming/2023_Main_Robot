// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.swrobotics.robot;

import java.util.ArrayList;
import java.util.HashMap;

import com.pathplanner.lib.PathConstraints;
import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.auto.SwerveAutoBuilder;
import com.swrobotics.lib.swerve.commands.DriveBlindCommand;
import com.swrobotics.lib.swerve.commands.TurnBlindCommand;
import com.swrobotics.lib.swerve.commands.TurnToAngleCommand;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.robot.blockauto.AutoBlocks;
import com.swrobotics.robot.commands.DefaultDriveCommand;
import com.swrobotics.robot.commands.FollowPathCommand;
import com.swrobotics.robot.commands.LightCommand;
import com.swrobotics.robot.control.Input;
import com.swrobotics.robot.control.InputSelector;
import com.swrobotics.robot.control.XboxInput;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.Lights;
import com.swrobotics.robot.subsystems.Vision;
import com.swrobotics.robot.subsystems.Lights.Color;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.PrintCommand;
import edu.wpi.first.wpilibj2.command.button.Button;

import com.swrobotics.mathlib.CWAngle;


/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in
 * the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of
 * the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
    private static final String MESSENGER_HOST_ROBOT = "10.21.29.3";
    private static final String MESSENGER_HOST_SIM = "localhost";
    private static final int MESSENGER_PORT = 5805;
    private static final String MESSENGER_NAME = "Robot";
    

    private final SendableChooser<Command> autoSelector;
    
    // The robot's subsystems and commands are defined here...
    public final DrivetrainSubsystem m_drivetrainSubsystem = new DrivetrainSubsystem();
    private final InputSelector inputSelector = new InputSelector(this);
    public final Lights m_lights = new Lights();
    public final Vision m_vision = new Vision(m_drivetrainSubsystem);

    private final MessengerClient messenger;

    // A bit of a hack so that it gets the command on auto init
    private final Command blockAutoCommand;

    /**
     * The container for the robot. Contains subsystems, OI devices, and commands.
     */
    public RobotContainer() {
        // Set up the default command for the drivetrain.
        // The controls are for field-oriented driving:
        // Left stick Y axis -> forward and backwards movement
        // Left stick X axis -> left and right movement
        // Right stick X axis -> rotation


        m_vision.register();
        inputSelector.register();

        // Initialize Messenger
        messenger = new MessengerClient(
                RobotBase.isSimulation() ? MESSENGER_HOST_SIM : MESSENGER_HOST_ROBOT,
                MESSENGER_PORT,
                MESSENGER_NAME
        );

        // Initialize block auto
        AutoBlocks.init(messenger, this);

        // Generate autos to choose from
        Command blankAuto = new InstantCommand();
        Command printAuto = new PrintCommand("Auto chooser is working!");

        Command justLights = new LightCommand(m_lights, Color.BLUE, 0.2).andThen(
            new LightCommand(m_lights, Color.GOLD, 1),
            new LightCommand(m_lights, Color.GREEN, 2),
            new LightCommand(m_lights, Color.WHITE, 3),
            new LightCommand(m_lights, Color.RAINBOW, 5.0)
        );

        // Command justLights = new LightTest(m_lights);

        // Generate drive commands using PathPlanner
        HashMap<String, Command> eventMap = new HashMap<>();

        // Add all of the colors as potential markers
        for (Color color : Color.values()) {
            // eventMap.put(color.name(), new PrintCommand("Color: " + color.name()));
            System.out.println("Add color: " + color.name());
            // eventMap.put(color.name(), new LightCommand(m_lights, color, 0.02));
            eventMap.put(color.name(), new CommandBase() {
                @Override
                public void initialize() {
                    m_lights.set(color);
                }
            });
        }

        eventMap.put("marker1", new PrintCommand("Passed marker 1"));

        SwerveAutoBuilder builder = m_drivetrainSubsystem.getAutoBuilder(eventMap);

        Command smallPathAuto = builder.fullAuto(getPath("Small Path"));
        Command bigPathAuto = builder.fullAuto(getPath("Big Path"));
        Command twentyOne = builder.fullAuto(getPath("2129"));
        Command lightShow = builder.fullAuto(getPath("Light Show"));
        Command tinyAuto = builder.fullAuto(getPath("Tiny Path"));
        Command doorToWindow = builder.fullAuto(getPath("Door to Window"));

        Command blindDrive = new DriveBlindCommand(this, CWAngle.deg(90), 1, 1, true);
        Command blindTurn = new TurnBlindCommand(this, Math.PI, 2.3);

        Command blindCombo = new ParallelCommandGroup(blindDrive, blindTurn);

        Command turnToAngle = new TurnToAngleCommand(this, CWAngle.deg(90), false).withTimeout(5);

        m_drivetrainSubsystem.showTrajectory(getPath("Door to Window").get(0));
        // m_drivetrainSubsystem.showTrajectory(getPath("Small Path").get(0));

        Command pathTest = new FollowPathCommand(m_drivetrainSubsystem, m_lights);

        blockAutoCommand = new InstantCommand();

        // Create a chooser to select the autonomous
        autoSelector= new SendableChooser<>();
        autoSelector.setDefaultOption("No Auto", blankAuto);
        autoSelector.addOption("Print Auto", printAuto);
        autoSelector.addOption("Small Path", smallPathAuto);
        autoSelector.addOption("Big Path", bigPathAuto);
        autoSelector.addOption("2129", twentyOne);
        autoSelector.addOption("Light Show", lightShow);
        autoSelector.addOption("Tiny Auto", tinyAuto);
        autoSelector.addOption("Door to Window", doorToWindow);
        autoSelector.addOption("Follow Path", pathTest);
        autoSelector.addOption("Just lights", justLights);
        autoSelector.addOption("Block Auto", blockAutoCommand);
        autoSelector.addOption("Blind drive", blindDrive);
        autoSelector.addOption("Blind combo", blindCombo);
        autoSelector.addOption("Turn to angle", turnToAngle);
        SmartDashboard.putData("Auto selector", autoSelector);
    }

    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand() {
        Command cmd = autoSelector.getSelected();
        
        // FIXME: Don't do this 
        if (cmd == blockAutoCommand) {
            System.out.println("Block autonomous detected, running command");
            return AutoBlocks.getSelectedAutoCommand();
        }
        
        return cmd;
    }

    private static ArrayList<PathPlannerTrajectory> getPath(String name) {
        return PathPlanner.loadPathGroup(name, new PathConstraints(0.2, 0.1)); // FIXME: Add throws and catch
    }

    public MessengerClient getMessenger() {
        return messenger;
    }
}