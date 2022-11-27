package com.swrobotics.lib.motor.ctre;

import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.ctre.phoenix.motorcontrol.TalonSRXSimCollection;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.lib.schedule.Subsystem;
import com.swrobotics.lib.wpilib.AbstractRobot;

import edu.wpi.first.wpilibj.RobotController;

/**
 * A class that improves the functionality of the TalonSRX
 */
public class TalonSRXMotor extends Motor {

    private static final int TIMEOUT_MS = 100;

    private final TalonSRX motor;
    private final TalonSRXSimCollection sim;

    public TalonSRXMotor(Subsystem parent, int id) {
        super(parent);

        motor = new TalonSRX(id);
        sim = motor.getSimCollection();

        // Reset the motor back to a stock configuration
        motor.configFactoryDefault(TIMEOUT_MS);
    }

    @Override
    protected void percentInternal(double percent) {
        motor.set(TalonSRXControlMode.PercentOutput, percent);
    }

    @Override
    public void periodic() {
        super.periodic();
        if (AbstractRobot.isSimulation()) {
            // Update the amount of power that the simulated motor is getting
            sim.setBusVoltage(RobotController.getBatteryVoltage());
        }
    }
}
