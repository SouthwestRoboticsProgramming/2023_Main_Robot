package com.swrobotics.robot;

import com.swrobotics.lib.drive.swerve.commands.DriveBlindCommand;
import com.swrobotics.lib.gyro.PigeonGyroscope;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.robot.commands.BalanceSequenceCommand;
import com.swrobotics.robot.commands.DefaultDriveCommand;
import com.swrobotics.robot.commands.ScoreSequenceCommand;
import com.swrobotics.robot.commands.arm.MoveArmToPositionCommand;
import com.swrobotics.robot.config.Settings;
import com.swrobotics.robot.input.Input;
import com.swrobotics.robot.subsystems.arm.ArmPosition;
import com.swrobotics.robot.subsystems.arm.ArmPositions;
import com.swrobotics.robot.subsystems.arm.ArmSubsystem;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.intake.GamePiece;
import com.swrobotics.robot.subsystems.intake.IntakeSubsystem;
import com.swrobotics.taskmanager.filesystem.FileSystemAPI;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
    // TODO: Endgame alert with rumble
    // TODO: Coast on decceleration
    // TODO: Characterization mode

    // Configuration for our Raspberry Pi communication service
    private static final String MESSENGER_HOST_ROBOT = "10.21.29.3";
    private static final String MESSENGER_HOST_SIM = "localhost";
    private static final int MESSENGER_PORT = 5805;
    private static final String MESSENGER_NAME = "Robot";

    private static final int PIGEON_CAN_ID = 20;

    // Create a way to choose between autonomous sequences
    private final SendableChooser<Supplier<Command>> autoSelector;

    // The robot's subsystems are defined here...
    public final Input input;

    public final DrivetrainSubsystem swerveDrive;

    public final ArmSubsystem arm;
    public final IntakeSubsystem intake;

    public final MessengerClient messenger;

    /**
     * The container for the robot. Contains subsystems, OI devices, and commands.
     */
    public RobotContainer() {
        // Turn off joystick warnings
        DriverStation.silenceJoystickConnectionWarning(Settings.getMode() == Settings.Mode.REAL);

        // Initialize Messenger
        String host = RobotBase.isSimulation() ? MESSENGER_HOST_SIM : MESSENGER_HOST_ROBOT;
//        String host = MESSENGER_HOST_ROBOT;
        messenger = new MessengerClient(host, MESSENGER_PORT, MESSENGER_NAME);

        new FileSystemAPI(messenger, "RoboRIO", Filesystem.getOperatingDirectory());

        intake = new IntakeSubsystem();
        arm = new ArmSubsystem(messenger, intake);

        swerveDrive = new DrivetrainSubsystem(new PigeonGyroscope(PIGEON_CAN_ID));
        input = new Input(this);
        swerveDrive.setDefaultCommand(new DefaultDriveCommand(swerveDrive, input));

        HashMap<String, Command> eventMap = new HashMap<>();

        // Put your events from PathPlanner here
        eventMap.put("BALANCE", new BalanceSequenceCommand(this, false));
        eventMap.put("BALANCE_REVERSE", new BalanceSequenceCommand(this, true));

        eventMap.put("SCORE_CUBE", new ScoreSequenceCommand(
                this, GamePiece.CUBE, ArmPositions.CUBE.scoreHigh,
                0.5, 1.0, CCWAngle.deg(100)));
//        eventMap.put("SCORE_CUBE_MID", new ScoreSequenceCommand(this, GamePiece.CUBE, ArmPositions.CUBE.scoreMid.front));

//        eventMap.put("SCORE_CONE", new ScoreSequenceCommand(this, GamePiece.CONE, ArmPositions.CONE.scoreHigh));

        putArmEvent(eventMap, "ARM_DEFAULT", ArmPositions.DEFAULT);
        putArmEventSet(eventMap, "ARM_CONE", ArmPositions.CONE);
        putArmEventSet(eventMap, "ARM_CUBE", ArmPositions.CUBE);
        swerveDrive.setAutoEvents(eventMap);

        // Print out all the registered events for debugging
        System.out.println("Registered auto events:");
        List<String> sortedKeys = new ArrayList<>(eventMap.keySet());
        sortedKeys.sort(String.CASE_INSENSITIVE_ORDER);
        for (String key : sortedKeys) {
            System.out.println("  - " + key);
        }

        // Autos that don't do anything
        Command blankAuto = new InstantCommand();

        // Autos to just drive off the line
        Command taxiSmart =
                swerveDrive.buildPathPlannerAuto("Taxi Auto"); // Drive forward and reset position
        Command taxiDumb =
                new DriveBlindCommand(
                                swerveDrive,
                                DrivetrainSubsystem.FIELD::getAllianceForwardAngle,
                                0.5,
                                false)
                        .withTimeout(2.0); // Just drive forward

        // Autos that just balance
        Command balanceWall = swerveDrive.buildPathPlannerAuto("Balance Wall");
        Command balanceBarrier = swerveDrive.buildPathPlannerAuto("Balance Barrier");
        Command balanceClose = new BalanceSequenceCommand(this, false);

        // Autos that score and then balance
        Command cubeMidBalance = swerveDrive.buildPathPlannerAuto("Cube Balance");
        Command coneMidBalance = swerveDrive.buildPathPlannerAuto("Cone Balance");
        Command cubeMidWallBalance = swerveDrive.buildPathPlannerAuto("Cube Wall Balance");
        Command coneMidWallBalance = swerveDrive.buildPathPlannerAuto("Cone Wall Balance");
        Command coneMidBalanceShort = swerveDrive.buildPathPlannerAuto("Cone Short Balance");
        Command cubeMidBalanceShort = swerveDrive.buildPathPlannerAuto("Cube Short Balance");

        // Advanced taxi autos that prepare us for next cycle
        Command getOfOfTheWayWall = swerveDrive.buildPathPlannerAuto("Get Out Of The Way Wall");
        Command getOfOfTheWayBarrier =
                swerveDrive.buildPathPlannerAuto("Get Out Of The Way Barrier");

        // Autos that do no balance but score
        Command cubeAndRunBarrier = swerveDrive.buildPathPlannerAuto("Cube and Run Barrier");
        Command cubeAndRunMid = swerveDrive.buildPathPlannerAuto("Cube and Run Mid");
        Command cubeAndRunWall = swerveDrive.buildPathPlannerAuto("Cube and Run Wall");

        // Autos that just do cube or cone mid

        // Create a chooser to select the autonomous
        autoSelector = new SendableChooser<>();
        autoSelector.addOption("Taxi Dumb", () -> taxiDumb);
        // autoSelector.addOption("Print Auto", () -> printAuto); Just for debugging

        // Balance Autos (15 / 12 pts)
        autoSelector.addOption("Balance Wall", () -> balanceWall);
        autoSelector.addOption("Balance Barrier", () -> balanceBarrier);
        autoSelector.addOption("Balance No Taxi", () -> balanceClose);

        // Score and balance barrier side (19 pts)
        autoSelector.addOption("Cube Balance", () -> cubeMidBalance);
        autoSelector.addOption("Cone Balance", () -> coneMidBalance);

        // Score and balance wall side
        autoSelector.addOption("Cube Wall Balance", () -> cubeMidWallBalance);
        autoSelector.addOption("Cone Wall Balance", () -> coneMidWallBalance);

        // Score and balance without taxi (16 pts)
        autoSelector.addOption("Cube Balance Short", () -> cubeMidBalanceShort);
        autoSelector.addOption("Cone Balance Short", () -> coneMidBalanceShort);

        // Prepare for next cycle without scoring (3 pts)
        autoSelector.addOption("Run Away Wall", () -> getOfOfTheWayWall);
        autoSelector.addOption("Run Away Barrier", () -> getOfOfTheWayBarrier);

        // Score cube then prepare for next cycle (7 pts)
        autoSelector.addOption("Cube and Run Barrier", () -> cubeAndRunBarrier);
        autoSelector.addOption("Cube and Run Center", () -> cubeAndRunMid);
        autoSelector.addOption("Cube and Run Wall", () -> cubeAndRunWall);

        // Autos that we would rather not use
        autoSelector.addOption("No Auto", () -> blankAuto);
        autoSelector.setDefaultOption("Taxi Smart", () -> taxiSmart);

        SmartDashboard.putData("Auto", autoSelector);
    }

    private void putArmEvent(Map<String, Command> eventMap, String name, ArmPosition.NT pos) {
        eventMap.put(name, new MoveArmToPositionCommand(this, pos));
    }

    private void putArmEventPair(Map<String, Command> eventMap, String prefix, ArmPositions.FrontBackPair pair) {
        putArmEvent(eventMap, prefix + "_FRONT", pair.front);
        putArmEvent(eventMap, prefix + "_BACK", pair.back);
    }

    private void putArmEventSet(Map<String, Command> eventMap, String prefix, ArmPositions.PositionSet set) {
        putArmEvent(eventMap, prefix + "_HIGH_FRONT", set.scoreHigh);
        putArmEventPair(eventMap, prefix + "_MID", set.scoreMid);
        putArmEventPair(eventMap, prefix + "_FLOOR", set.floorPickup);
        putArmEventPair(eventMap, prefix + "_CHUTE", set.chutePickup);
        putArmEventPair(eventMap, prefix + "_SUBSTATION", set.substationPickup);
    }

    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand() {
        return autoSelector.getSelected().get();
    }
}
