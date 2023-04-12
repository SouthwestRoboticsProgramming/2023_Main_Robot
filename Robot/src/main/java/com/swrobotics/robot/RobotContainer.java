package com.swrobotics.robot;

import com.pathplanner.lib.PathConstraints;
import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.PathPoint;
import com.pathplanner.lib.auto.SwerveAutoBuilder;
import com.swrobotics.lib.swerve.commands.DriveBlindCommand;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.robot.commands.BalanceSequenceCommand;
import com.swrobotics.robot.commands.DefaultDriveCommand;
import com.swrobotics.robot.commands.arm.MoveArmToPositionCommand;
import com.swrobotics.robot.input.Input;
import com.swrobotics.robot.positions.ArmPositions;
import com.swrobotics.robot.subsystems.Lights;
import com.swrobotics.robot.subsystems.arm.ArmSubsystem;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.drive.Pathfinder;
import com.swrobotics.robot.subsystems.intake.GamePiece;
import com.swrobotics.robot.subsystems.intake.IntakeSubsystem;
import com.swrobotics.robot.subsystems.vision.Limelight;
import com.swrobotics.robot.subsystems.vision.Photon;
import com.swrobotics.taskmanager.filesystem.FileSystemAPI;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.PrintCommand;
import edu.wpi.first.wpilibj2.command.SelectCommand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  private enum ScoreHeight {
    TOP,
    MID,
    BOTTOM
  }

  // Configuration for our Raspberry Pi communication service
  private static final String MESSENGER_HOST_ROBOT = "10.21.29.3";
  private static final String MESSENGER_HOST_SIM = "localhost";
  private static final int MESSENGER_PORT = 5805;
  private static final String MESSENGER_NAME = "Robot";

  // Create a way to choose between autonomous sequences
  private final SendableChooser<Supplier<Command>> autoSelector;

  // The robot's subsystems are defined here...
  public final Input input;
  public final DrivetrainSubsystem drivetrainSubsystem = new DrivetrainSubsystem();
  public final Pathfinder pathfinder;

  public final Photon photon = new Photon(this);
  public final Limelight limelight = null; // new Limelight();

  public final ArmSubsystem arm;
  public final IntakeSubsystem intake = new IntakeSubsystem();

  public final Lights lights = new Lights();

  public final MessengerClient messenger;

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Turn off joystick warnings
    DriverStation.silenceJoystickConnectionWarning(true);

    // Initialize Messenger
    messenger =
        new MessengerClient(
            RobotBase.isSimulation() ? MESSENGER_HOST_SIM : MESSENGER_HOST_ROBOT,
            MESSENGER_PORT,
            MESSENGER_NAME);

    new FileSystemAPI(messenger, "RoboRIO", Filesystem.getOperatingDirectory());
    arm = new ArmSubsystem(messenger);

    // Initialize pathfinder to be able to drive to any point on the field
    pathfinder = new Pathfinder(messenger, drivetrainSubsystem);

    input = new Input(this);
    drivetrainSubsystem.setDefaultCommand(new DefaultDriveCommand(drivetrainSubsystem, input));

    HashMap<String, Command> eventMap = new HashMap<>();

    SendableChooser<ScoreHeight> positionSelector = new SendableChooser<>();

    positionSelector.setDefaultOption("High", ScoreHeight.TOP);
    positionSelector.addOption("Mid", ScoreHeight.MID);
    positionSelector.addOption("Low", ScoreHeight.BOTTOM);

    SmartDashboard.putData("Auto Position", positionSelector);

    Command cubeLow =
        Commands.runOnce(() -> intake.setExpectedPiece(GamePiece.CUBE), intake)
            .andThen(
                new MoveArmToPositionCommand(this, () -> ArmPositions.DEFAULT.getTranslation()),
                Commands.run(intake::eject, intake).withTimeout(1.0));

    Command coneLow =
        Commands.runOnce(() -> intake.setExpectedPiece(GamePiece.CONE), intake)
            .andThen(
                new MoveArmToPositionCommand(this, () -> ArmPositions.DEFAULT.getTranslation()),
                Commands.run(intake::eject, intake).withTimeout(1.0));

    Command cubeMid =
        Commands.runOnce(() -> intake.setExpectedPiece(GamePiece.CUBE), intake)
            .andThen(
                new MoveArmToPositionCommand(
                    this,
                    () -> new Translation2d(0.6, ArmPositions.CUBE_CENTER.getTranslation().getY())),
                new MoveArmToPositionCommand(this, () -> ArmPositions.CUBE_CENTER.getTranslation()),
                Commands.run(intake::eject, intake).withTimeout(1.0));

    Command cubeHigh =
        Commands.runOnce(() -> intake.setExpectedPiece(GamePiece.CUBE), intake)
            .andThen(
                new MoveArmToPositionCommand(
                    this,
                    () -> new Translation2d(0.6, ArmPositions.CUBE_UPPER.getTranslation().getY())),
                new MoveArmToPositionCommand(this, () -> ArmPositions.CUBE_UPPER.getTranslation()),
                Commands.run(intake::eject, intake).withTimeout(1.0));

    Command coneMid =
        Commands.runOnce(() -> intake.setExpectedPiece(GamePiece.CONE), intake)
            .andThen(
                new MoveArmToPositionCommand(
                    this,
                    () -> new Translation2d(0.6, ArmPositions.CONE_CENTER.getTranslation().getY())),
                new MoveArmToPositionCommand(this, () -> ArmPositions.CONE_CENTER.getTranslation()),
                Commands.run(intake::eject, intake).withTimeout(1.0));

    Command coneHigh =
        Commands.runOnce(() -> intake.setExpectedPiece(GamePiece.CONE), intake)
            .andThen(
                new MoveArmToPositionCommand(
                    this,
                    () -> new Translation2d(0.6, ArmPositions.CONE_UPPER.getTranslation().getY())),
                new MoveArmToPositionCommand(this, () -> ArmPositions.CONE_UPPER.getTranslation()),
                Commands.run(intake::eject, intake).withTimeout(1.0));

    Command scoreCone =
        new SelectCommand(
            Map.ofEntries(
                Map.entry(ScoreHeight.TOP, coneHigh),
                Map.entry(ScoreHeight.MID, coneMid),
                Map.entry(ScoreHeight.BOTTOM, coneLow)),
            positionSelector::getSelected);

    Command scoreCube =
        new SelectCommand(
            Map.ofEntries(
                Map.entry(ScoreHeight.TOP, cubeHigh),
                Map.entry(ScoreHeight.MID, cubeMid),
                Map.entry(ScoreHeight.BOTTOM, cubeLow)),
            positionSelector::getSelected);

    // Put your events from PathPlanner here
    eventMap.put("BALANCE", new BalanceSequenceCommand(this, false));
    eventMap.put("BALANCE_REVERSE", new BalanceSequenceCommand(this, true));

    eventMap.put("SCORE_CUBE", scoreCube);
    eventMap.put("SCORE_CONE", scoreCone);

    eventMap.put(
        "ARM_DEFAULT", new MoveArmToPositionCommand(this, ArmPositions.DEFAULT::getTranslation));
    // eventMap.put("ARM_DEFAULT", new PrintCommand("it work"));

    // Allow for easy creation of autos using PathPlanner
    SwerveAutoBuilder builder = drivetrainSubsystem.getAutoBuilder(eventMap);

    // Autos that don't do anything
    Command blankAuto = new InstantCommand();
    Command printAuto = new PrintCommand("Auto chooser is working!");

    // Autos to just drive off the line
    Command taxiSmart = builder.fullAuto(getPath("Taxi Auto")); // Drive forward and reset position
    Command taxiDumb =
        new DriveBlindCommand(this, DrivetrainSubsystem::getAllianceForward, 0.5, false)
            .withTimeout(2.0); // Just drive forward

    // Autos that just balance
    Command balanceWall = builder.fullAuto(getPath("Balance Wall"));
    Command balanceBarrier = builder.fullAuto(getPath("Balance Barrier"));
    Command balanceClose = new BalanceSequenceCommand(this, false);

    // Autos that score and then balance
    Command cubeMidBalance = builder.fullAuto(getPath("Cube Balance"));
    Command coneMidBalance = builder.fullAuto(getPath("Cone Balance"));
    Command cubeMidWallBalance = builder.fullAuto(getPath("Cube Wall Balance"));
    Command coneMidWallBalance = builder.fullAuto(getPath("Cone Wall Balance"));
    Command coneMidBalanceShort = builder.fullAuto(getPath("Cone Short Balance"));
    Command cubeMidBalanceShort = builder.fullAuto(getPath("Cube Short Balance"));

    // Advanced taxi autos that prepare us for next cycle
    Command getOfOfTheWayWall = builder.fullAuto(getPath("Get Out Of The Way Wall"));
    Command getOfOfTheWayBarrier = builder.fullAuto(getPath("Get Out Of The Way Barrier"));

    // Autos that do no balance but score
    Command cubeAndRunBarrier = builder.fullAuto(getPath("Cube and Run Barrier"));
    Command cubeAndRunMid = builder.fullAuto(getPath("Cube and Run Mid"));
    Command cubeAndRunWall = builder.fullAuto(getPath("Cube and Run Wall"));

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

    // Just score and don't move (4 pts)
    autoSelector.addOption("Just Cube", () -> scoreCube);
    autoSelector.addOption("Just Cone", () -> scoreCone);

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

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return autoSelector.getSelected().get();
  }

  private static List<PathPlannerTrajectory> getPath(String name) {
    List<PathPlannerTrajectory> path =
        PathPlanner.loadPathGroup(name, new PathConstraints(2.0, 1.0));
    if (path != null) {
      return path;
    }

    System.out.println("Could not find that path, using default path instead");

    // Generate a blank path
    path = new ArrayList<>();
    path.add(
        PathPlanner.generatePath(
            new PathConstraints(1.0, 1.0),
            new ArrayList<PathPoint>() {
              {
                add(new PathPoint(new Translation2d(), new Rotation2d()));
                add(new PathPoint(new Translation2d(1.0, 0), new Rotation2d()));
              }
            }));
    return path;
  }
}
