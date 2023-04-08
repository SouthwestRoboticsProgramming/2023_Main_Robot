package com.swrobotics.lib.motor.ctre;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;

// TODO: Remote feedback sensors
public final class TalonSRXMotor extends TalonMotor {
    private static TalonSRX createSRX(int id) {
        TalonSRXConfiguration config = new TalonSRXConfiguration();

        // Initially configure no feedback device, can be set later with enableIntegratedEncoder()
        config.primaryPID.selectedFeedbackSensor = FeedbackDevice.None;

        // Clear PID constants
        config.slot0.kP = 0;
        config.slot0.kI = 0;
        config.slot0.kD = 0;
        config.slot0.kF = 0;

        TalonSRX srx = new TalonSRX(id);
        srx.configAllSettings(config);

        return srx;
    }

    public TalonSRXMotor(int canID) {
        super(createSRX(canID));
    }

    @Override
    protected boolean canSetSensorPhase() {
        return true;
    }

    public TalonSRXMotor enableIntegratedEncoder(FeedbackDevice device, int ticksPerRotation) {
        TalonSRX srx = (TalonSRX) talon;
        srx.configSelectedFeedbackSensor(device);
        enableIntegratedEncoder(ticksPerRotation);
        return this;
    }
}
