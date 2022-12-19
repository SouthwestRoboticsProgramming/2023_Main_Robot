package com.swrobotics.lib.motor.ctre;

import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXSimCollection;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.lib.schedule.Subsystem;
import com.swrobotics.lib.wpilib.AbstractRobot;

import edu.wpi.first.hal.can.CANStatus;
import edu.wpi.first.wpilibj.RobotController;

public class TalonFXMotor extends Motor {
    private static final int TIMEOUT_MS = 100;
    
    private final TalonFX motor;
    private final TalonFXSimCollection sim;

    public TalonFXMotor(Subsystem parent, int id) {
        super(parent);
        motor = new TalonFX(id);
        sim = motor.getSimCollection();
    }

    @Override
    protected void percentInternal(double percent) {
        motor.set(TalonFXControlMode.PercentOutput, percent);
    }

    @Override
    public void periodic() {
        super.periodic();
        if (AbstractRobot.isSimulation()) {
            sim.setBusVoltage(RobotController.getBatteryVoltage());
        }
    }
}
