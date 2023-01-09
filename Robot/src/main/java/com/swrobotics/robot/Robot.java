package com.swrobotics.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.PrintCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

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
    private Command m_autonomousCommand;
    private final Timer m_autonomousTimer = new Timer();

    private RobotContainer m_robotContainer;

    /**
     * This function is run when the robot is first started up and should be used
     * for any
     * initialization code.
     */
    @Override
    public void robotInit() {
        // Instantiate our RobotContainer. This will perform all our button bindings,
        // and put our
        // autonomous chooser on the dashboard.
        m_robotContainer = new RobotContainer();
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

        m_robotContainer.getMessenger().readMessages();
    }

    /**
     * This function is called once each time the robot enters Disabled mode.
     */
    @Override
    public void disabledInit() {
    }

    @Override
    public void disabledPeriodic() {
    }

    /**
     * This autonomous runs the autonomous command selected by your
     * {@link RobotContainer} class.
     */
    @Override
    public void autonomousInit() {
        // If an autonomous command has already be set, reset it
        if (m_autonomousCommand != null) {
            SequentialCommandGroup.clearGroupedCommands();
            m_autonomousCommand.cancel();
            System.out.println("Canceled the current auto command");
        }

        // Get autonomous from selector
        m_autonomousCommand = m_robotContainer.getAutonomousCommand();
        System.out.println(m_autonomousCommand);

        // schedule the autonomous command (example)
        if (m_autonomousCommand != null) {
            SequentialCommandGroup finalCommand = m_autonomousCommand.andThen(
                    new PrintCommand("Auto Completed!"),
                    new CommandBase() {
                        @Override
                        public void execute() {
                            m_autonomousTimer.stop();
                            System.out.printf("Auto finished in %.3f seconds\n", m_autonomousTimer.get());
                        }

                        @Override
                        public boolean isFinished() {
                            return true;
                        }

                    });
            // m_autonomousCommand.schedule();
            finalCommand.schedule();
            m_autonomousTimer.stop();
            m_autonomousTimer.reset();
            m_autonomousTimer.start();
            // CommandScheduler.getInstance().schedule(m_autonomousCommand);
        }
    }
}