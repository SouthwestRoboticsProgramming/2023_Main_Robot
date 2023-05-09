package com.swrobotics.robot;

import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.lib.motor.ctre.TalonSRXMotor;
import com.swrobotics.lib.motor.rev.NEOMotor;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTInteger;
import com.swrobotics.mathlib.CWAngle;
import edu.wpi.first.wpilibj.TimedRobot;

public class Robot extends TimedRobot {
    private static final NTDouble motorSpeed1 = new NTDouble("testbench/motor1/motor speed", 0);

    private static final NTInteger demandType = new NTInteger("testbench/motor2/demand", 0);
    private static final NTDouble motorSpeed2 = new NTDouble("testbench/motor2/motor speed", 0);
    private static final NTDouble encPos = new NTDouble("testbench/motor2/encoder pos", 0);
    private static final NTDouble encVel = new NTDouble("testbench/motor2/encoder vel", 0);
    private static final NTDouble kP = new NTDouble("testbench/motor2/kP", 0);
    private static final NTDouble kI = new NTDouble("testbench/motor2/kI", 0);
    private static final NTDouble kD = new NTDouble("testbench/motor2/kD", 0);
    private static final NTDouble kF = new NTDouble("testbench/motor2/kF", 0);

    private final Motor motor1 = new TalonSRXMotor(11);
    private final FeedbackMotor motor2 = new NEOMotor(8);
    private final Encoder enc = motor2.getIntegratedEncoder();

    @Override
    public void robotInit() {
        motor2.setPIDF(kP, kI, kD, kF);
    }

    @Override
    public void robotPeriodic() {
        motor1.setPercentOut(motorSpeed1.get());

        double demand = motorSpeed2.get();
        switch (demandType.get()) {
            case 0: motor2.setPercentOut(demand); break;
            case 1: motor2.setPosition(CWAngle.rot(demand)); break;
            case 2: motor2.setVelocity(CWAngle.rot(demand)); break;
        }

        encPos.set(enc.getAngle().cw().rot());
        encVel.set(enc.getVelocity().cw().rot());
    }
}
