package com.swrobotics.robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

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
import com.swrobotics.robot.input.ButtonPanel;
import com.swrobotics.robot.subsystems.arm.ArmSubsystem;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.Lights;
import com.swrobotics.robot.subsystems.drive.Pathfinder;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
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
    private final SendableChooser<Supplier<Command>> autoSelector;

    // The robot's subsystems are defined here...
    public final DrivetrainSubsystem drivetrainSubsystem = new DrivetrainSubsystem();
    public final Pathfinder pathfinder;

    public final ArmSubsystem arm;

    public final Lights lights = new Lights();
    public final StatusLogging statuslogger = new StatusLogging(lights);

    private final XboxController controller = new XboxController(0);
    private final ButtonPanel buttonPanel;

    private final MessengerClient messenger;

    /**
     * The container for the robot. Contains subsystems, OI devices, and commands.
     */
    public RobotContainer() {
        // Turn off joystick warnings
        DriverStation.silenceJoystickConnectionWarning(true);

        // Set up the default command for the drivetrain.
        // The controls are for field-oriented driving:
        // Left stick Y axis -> forward and backwards movement
        // Left stick X axis -> left and right movement
        // Right stick X axis -> rotation
        drivetrainSubsystem.setDefaultCommand(new DefaultDriveCommand(
                drivetrainSubsystem,
                () -> -modifyAxis(controller.getLeftY()) * DrivetrainSubsystem.MAX_ACHIEVABLE_VELOCITY_METERS_PER_SECOND,
                () -> -modifyAxis(controller.getLeftX()) * DrivetrainSubsystem.MAX_ACHIEVABLE_VELOCITY_METERS_PER_SECOND,
                () -> -modifyAxis(controller.getRightX())
                        * DrivetrainSubsystem.MAX_ANGULAR_VELOCITY_RADIANS_PER_SECOND));

        // Configure the rest of the button bindings
        configureButtonBindings();

        // Initialize Messenger
        messenger = new MessengerClient(
                RobotBase.isSimulation() ? MESSENGER_HOST_SIM : MESSENGER_HOST_ROBOT,
                MESSENGER_PORT,
                MESSENGER_NAME
        );
        buttonPanel = new ButtonPanel(messenger);
        arm = new ArmSubsystem(messenger);

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

        // Create a chooser to select the autonomous
        autoSelector = new SendableChooser<>();
        autoSelector.setDefaultOption("Taxi Dumb", () -> taxiDumb);
        autoSelector.addOption("No Auto", () -> blankAuto);
        autoSelector.addOption("Print Auto", () -> printAuto);
        autoSelector.addOption("Taxi Smart", () -> taxiSmart);
        autoSelector.addOption("Block Auto", AutoBlocks::getSelectedAutoCommand);

        SmartDashboard.putData("Auto", autoSelector);
    }

    /**
     * Use this method to define your button->command mappings. Buttons can be
     * created by
     * instantiating a {@link GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing
     * it to a {@link
     * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
     */
    private void configureButtonBindings() {
        // Back button zeros the gyroscope
        new Trigger(controller::getBackButton)
                // No requirements because we don't need to interrupt anything
                .onTrue(Commands.runOnce(() -> drivetrainSubsystem.zeroGyroscope()));

        // Start button does leveling sequence on charger
        new Trigger(controller::getStartButton)
                .whileTrue(new AutoBalanceCommand(this));

        // FIXME: Probably wrong buttons
        new Trigger(() -> buttonPanel.isButtonDown(1, 3))
                .onTrue(Commands.runOnce(() -> lights.set(Lights.Color.BLUE)));
        new Trigger(() -> buttonPanel.isButtonDown(2, 3))
                .onTrue(Commands.runOnce(() -> lights.set(Lights.Color.YELLOW)));
    }

    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand() {
        return autoSelector.getSelected().get();
    }

    private static double deadband(double value, double deadband) {
        if (Math.abs(value) > deadband) {
            if (value > 0.0) {
                return (value - deadband) / (1.0 - deadband);
            } else {
                return (value + deadband) / (1.0 - deadband);
            }
        } else {
            return 0.0;
        }
    }

    private static double modifyAxis(double value) {
        // Deadband
        value = deadband(value, 0.15);

        // Square the axis
        value = Math.copySign(value * value, value);

        return value;
    }

    private static List<PathPlannerTrajectory> getPath(String name) {

        List<PathPlannerTrajectory> path = PathPlanner.loadPathGroup(name, new PathConstraints(2.0, 1.0));
        if (path != null) {
            return path;
        }

        System.out.println("Could not find that path, using default path instead");

        // // Generate a blank path
        path = new ArrayList<>();
        path.add(PathPlanner.generatePath(new PathConstraints(1.0, 1.0), new ArrayList<PathPoint>() {{
            add(new PathPoint(new Translation2d(), new Rotation2d()));
            add(new PathPoint(new Translation2d(1.0, 0), new Rotation2d()));
        }}));
        return path;
    }

    public MessengerClient getMessenger() {
        return messenger;
    }
}
