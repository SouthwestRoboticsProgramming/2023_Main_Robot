package com.swrobotics.robot;

import com.swrobotics.lib.ThreadUtils;
import com.swrobotics.robot.commands.arm.ManualArmControlCommand;
import com.swrobotics.robot.positions.ScoringPositions;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.PrintCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the
 * name of this class or
 * the package after creating this project, you must also update the
 * build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
    private Command autonomousCommand;
    private final Timer autonomousTimer = new Timer();

    private RobotContainer robotContainer;

    /**
     * This function is run when the robot is first started up and should be used
     * for any
     * initialization code.
     */
    @Override
    public void robotInit() {
        // Create a RobotContainer to manage our subsystems and our buttons
        robotContainer = new RobotContainer(this);
    }

    /**
     * This function is called every robot packet, no matter the mode. Use this for
     * items like
     * diagnostics that you want ran during disabled, autonomous, teleoperated and
     * test.
     *
     * <p>
     * This runs after the mode specific periodic functions, but before LiveWindow
     * and
     * SmartDashboard integrated updating.
     */
    @Override
    public void robotPeriodic() {
        // Runs the Scheduler. This is responsible for polling buttons, adding
        // newly-scheduled
        // commands, running already-scheduled commands, removing finished or
        // interrupted commands,
        // and running subsystem periodic() methods. This must be called from the
        // robot's periodic
        // block in order for anything in the Command-based framework to work.
        CommandScheduler.getInstance().run();

        // Run all operations queued to run on main thread
        ThreadUtils.runMainThreadOperations();

        // Handle messages being sent by the raspberry pi
        robotContainer.getMessenger().readMessages();

        // Update the scoring positions since our alliance could change while running
        ScoringPositions.update();
    }

    /**
     * This function is called once each time the robot enters Disabled mode.
     */
    @Override
    public void disabledInit() {}

    @Override
    public void disabledPeriodic() {}

    /**
     * This autonomous runs the autonomous command selected by your
     * {@link RobotContainer} class.
     */
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
            // Follow up autonomous command with diagnostics on how quickly it ran
            // autonomousCommand = autonomousCommand.andThen(
            //         new PrintCommand("Auto Completed!"),
            //         new RunCommand(() -> autonomousTimer.stop()));
            //         new RunCommand(() -> System.out.printf("Auto finished in %.3f seconds\n", autonomousTimer.get())
            // );

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