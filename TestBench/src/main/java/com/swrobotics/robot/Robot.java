package com.swrobotics.robot;

import com.swrobotics.lib.ThreadUtils;
import com.swrobotics.lib.motor.ctre.PWMVictorSPMotor;
import com.swrobotics.lib.motor.ctre.TalonFXMotor;
import com.swrobotics.lib.motor.ctre.TalonSRXMotor;
import com.swrobotics.lib.motor.rev.BrushedSparkMaxMotor;
import com.swrobotics.lib.motor.rev.NEOMotor;
import com.swrobotics.robot.autotest.motor.MotorTest;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.PrintCommand;

public class Robot extends TimedRobot {
    // TODO: Make commands to automatically test all the features

    /*
    Things to test:

    Key:
      _ = untested
      X = tested & working
      . = unsupported feature
      / = test needs hardware we don't have

    CanCoder:
      - Correct units and direction:
        - [_] Absolute position
        - [_] Absolute velocity
        - [_] Relative position
        - [_] Relative velocity
      - [_] Relative set works
      - [_] Invert works

     NavX:
       - [_] Correct units and direction
       - [_] Determine what pitch, yaw, and roll are physically, and document that

     Motors:
                             TalonFX  TalonSRX  MAX NEO  MAX NEO550  MAX brush  Victor SP/Any PWM
     Enc. pos units and dir  [_]      [_]       [X]      [X]         [_]         .
     Enc. vel units and dir  [_]      [_]       [X]      [X]         [_]         .
     Enc. set position       [_]      [_]       [X]      [X]         [_]         .                 Auto tested
     Enc. set phase           .       [_]        .        .          [/]         .                 Auto tested
     Invert preserves phase  [_]      [_]       [X]      [X]         [/]         .                 Auto tested
     Pos pct out is CCW      [_]      [X]       [X]      [X]         [_]        [X]                Auto tested
     Inverting output works  [_]      [X]       [X]      [X]         [_]        [X]                Auto tested
     Integrated PID pos      [_]      [_]       [X]      [X]         [/]         .
     Integrated PID vel      [_]      [_]       [X]      [X]         [/]         .
     Follow matching         [_]      [_]       [_]      [_]         [_]         .
     Follow inverted         [_]      [_]       [_]      [_]         [_]         .
     Remote sensor           [_]      [_]        .        .           .          .
     Encoder on data port    [_]      [_]       [/]      [/]         [/]         .
     */

    private static final int CAN_ID_SPARK_MAX = 8;
    private static final int CAN_ID_SRX = 11;

    private final NEOMotor neo1 = new NEOMotor(CAN_ID_SPARK_MAX);
    private final NEOMotor neo2 = new NEOMotor(23);
    private final TalonSRXMotor srx = new TalonSRXMotor(CAN_ID_SRX);
    private final PWMVictorSPMotor sp = new PWMVictorSPMotor(0);
    private final TalonFXMotor fx = new TalonFXMotor(4);

    private Command testSequence;

    @Override
    public void robotInit() {
        new ManualMotorController("neo550", neo1, false, true);
        new ManualMotorController("big neo", neo2, false, true);
        new ManualMotorController("talon srx", srx, true, true);
        new ManualMotorController("victor sp", sp, false, false);
        new ManualMotorController("falcon", fx, false, true);
    }

    @Override
    public void autonomousInit() {
        testSequence = Commands.sequence(
                new PrintCommand("Begin test sequence"),
                new MotorTest(neo1, "NEO 550", true, false),
                new MotorTest(neo2, "Big NEO", true, false),
                new MotorTest(srx, "SRX", false, true),
                new MotorTest(sp, "SP", false, false),
                new MotorTest(fx, "Falcon", true, false),
                new PrintCommand("End test sequence")
        );
        testSequence.schedule();
    }

    @Override
    public void autonomousExit() {
        testSequence.cancel();
    }

    @Override
    public void robotPeriodic() {
        CommandScheduler.getInstance().run();
        ThreadUtils.runMainThreadOperations();
    }
}
