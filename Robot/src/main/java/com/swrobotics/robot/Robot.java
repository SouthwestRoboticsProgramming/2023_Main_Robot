package com.swrobotics.robot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

import org.littletonrobotics.junction.LogFileUtil;
import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggedPowerDistribution;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGReader;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

import com.swrobotics.robot.Settings.RobotType;
import com.swrobotics.robot.Settings.Mode;

import edu.wpi.first.hal.AllianceStationID;
import edu.wpi.first.wpilibj.Threads;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

public class Robot extends LoggedRobot {
    private Command autonomousCommand;
    private final Timer autonomousTimer = new Timer();

    private RobotContainer robotContainer;

    @Override
    public void robotInit() {
        // Configure logging
        Logger logger = Logger.getInstance();

        // Record metadata so that the logs have more to work off of
        logger.recordMetadata("ProjectName", BuildConstants.MAVEN_NAME);
        logger.recordMetadata("BuildDate", BuildConstants.BUILD_DATE);
        logger.recordMetadata("GitSHA", BuildConstants.GIT_SHA);
        logger.recordMetadata("GitDate", BuildConstants.GIT_DATE);
        logger.recordMetadata("GitBranch", BuildConstants.GIT_BRANCH);
        switch (BuildConstants.DIRTY) {
            case 0:
                logger.recordMetadata("GitDirty", "All changes committed");
                break;
            case 1:
                logger.recordMetadata("GitDirty", "Uncomitted changes");
                break;
            default:
                logger.recordMetadata("GitDirty", "Unknown");
                break;
        }

        // Set up data receivers & replay source
        switch (Settings.getMode()) {
            case REAL:
                String folder = Settings.logFolders.get(Settings.robot);
                if (folder != null) {
                    logger.addDataReceiver(new WPILOGWriter(folder));
                }
                logger.addDataReceiver(new NT4Publisher());
                if (Settings.robot == RobotType.COMPETITION) {
                    LoggedPowerDistribution.getInstance(50, ModuleType.kAutomatic); // FIXME: Correct ID
                }
                break;

            case SIMULATION:
                logger.addDataReceiver(new NT4Publisher());
                break;

            case REPLAY:
                String path = LogFileUtil.findReplayLog();
                logger.setReplaySource(new WPILOGReader(path));
                logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(path, "_sim")));
                break;
        }

        // Start AdvantageKit logger
        setUseTiming(Settings.getMode() != Mode.REPLAY);
        logger.start();

        // Create a RobotContainer to manage our subsystems and our buttons
        robotContainer = new RobotContainer();

        // Log active commands
        Map<String, Integer> commandCounts = new HashMap<>();
        BiConsumer<Command, Boolean> logCommandFunction = (Command command, Boolean active) -> {
            String name = command.getName();
            int count = commandCounts.getOrDefault(name, 0) + (active ? 1 : -1);
            commandCounts.put(name, count);
            Logger.getInstance()
                    .recordOutput(
                            "CommandsUnique/" + name + "_" + Integer.toHexString(command.hashCode()), active);
            Logger.getInstance().recordOutput("CommandsAll/" + name, count > 0);
        };
        CommandScheduler.getInstance()
                .onCommandInitialize(
                        (Command command) -> {
                            logCommandFunction.accept(command, true);
                        });
        CommandScheduler.getInstance()
                .onCommandFinish(
                        (Command command) -> {
                            logCommandFunction.accept(command, false);
                        });
        CommandScheduler.getInstance()
                .onCommandInterrupt(
                        (Command command) -> {
                            logCommandFunction.accept(command, false);
                        });

        // Random alliance in sim (this makes sure that things work on both sides)
        if (Settings.getMode() == Mode.SIMULATION) {
            if (new Random().nextBoolean()) {
                DriverStationSim.setAllianceStationId(AllianceStationID.Blue1);
            }
        }
    }

    @Override
    public void robotPeriodic() {
        Threads.setCurrentThreadPriority(true, 99);

        robotContainer.messenger.readMessages();
        CommandScheduler.getInstance().run(); // Leave this alone
    }

    @Override
    public void autonomousInit() {
        // If an autonomous command has already be set, reset it
        if (autonomousCommand != null) {
            autonomousCommand.cancel();
            System.out.println("Canceled the current auto command");
        }

        // Get autonomous from selector
        autonomousCommand = robotContainer.getAutonomousCommand();

        // schedule the autonomous command (example)
        if (autonomousCommand != null) {
            autonomousCommand.schedule();

            // Reset the timer
            autonomousTimer.reset();
            autonomousTimer.start();
        }
    }

    @Override
    public void autonomousExit() {
        if (autonomousCommand != null) {
            autonomousCommand.cancel();
        }
    }
}
