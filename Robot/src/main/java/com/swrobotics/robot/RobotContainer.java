package com.swrobotics.robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.pathplanner.lib.PathConstraints;
import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.PathPoint;
import com.pathplanner.lib.auto.SwerveAutoBuilder;
import com.swrobotics.lib.swerve.commands.DriveBlindCommand;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.robot.blockauto.AutoBlocks;
import com.swrobotics.robot.blockauto.WaypointStorage;
import com.swrobotics.robot.commands.AutoBalanceCommand;
import com.swrobotics.robot.commands.DefaultDriveCommand;

import com.swrobotics.robot.commands.FollowPathCommand;
import com.swrobotics.robot.commands.LightCommand;
import com.swrobotics.robot.control.Input;
import com.swrobotics.robot.control.InputSelector;
import com.swrobotics.robot.control.XboxInput;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.Lights;
import com.swrobotics.robot.subsystems.Pathfinder;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.PrintCommand;
import com.swrobotics.robot.subsystems.StatusLogging;


import com.swrobotics.mathlib.CWAngle;
import edu.wpi.first.wpilibj2.command.button.Trigger;


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
    // Configuration for our Raspberry Pi communication service
    private static final String MESSENGER_HOST_ROBOT = "10.21.29.3";
    private static final String MESSENGER_HOST_SIM = "localhost";
    private static final int MESSENGER_PORT = 5805;
    private static final String MESSENGER_NAME = "Robot";
    

    // Create a way to choose between autonomous sequences
    private final SendableChooser<Command> autoSelector;

    
    // The robot's subsystems and commands are defined here...
    public final DrivetrainSubsystem m_drivetrainSubsystem = new DrivetrainSubsystem();
    private final InputSelector inputSelector = new InputSelector(this);
    public final Lights m_lights = new Lights();
    public final Vision m_vision = new Vision(m_drivetrainSubsystem);


    private final MessengerClient messenger;

    // A bit of a hack so that it gets the command on auto init FIXME-@rmheuer: Un-hackify this
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
        WaypointStorage.init(messenger);

        // Initialize pathfinder to be able to drive to any point on the field
        pathfinder = new Pathfinder(messenger, drivetrainSubsystem);

        HashMap<String, Command> eventMap = new HashMap<>();

        // Put your events from PathPlanner here
        eventMap.put("marker1", new PrintCommand("Passed marker 1"));

        // Allow for easy creation of autos using PathPlanner
        SwerveAutoBuilder builder = drivetrainSubsystem.getAutoBuilder(eventMap);

        // Add your pre-generated autos here...
        Command blankAuto = new InstantCommand();
        Command printAuto = new PrintCommand("Auto chooser is working!");

        // Autos to just drive off the line
        Command taxiSmart = builder.fullAuto(getPath("Taxi Auto"));     // Drive forward and reset position
        Command taxiDumb = new DriveBlindCommand(this, Angle.ZERO, 0.5, true).withTimeout(2.0); // Just drive forward

        blockAutoCommand = new InstantCommand();

        // Create a chooser to select the autonomous
        autoSelector = new SendableChooser<>();
        autoSelector.setDefaultOption("Taxi Dumb", taxiDumb);
        autoSelector.addOption("No Auto", blankAuto);
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
