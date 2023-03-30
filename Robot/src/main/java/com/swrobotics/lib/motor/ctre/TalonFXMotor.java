package com.swrobotics.lib.motor.ctre;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;

public final class TalonFXMotor extends TalonMotor {
    private static TalonFX createFX(int canID, String canBus) {
        TalonFXConfiguration config = new TalonFXConfiguration();

        // Set initial PIDF to zero so the motor won't move by default
        config.slot0.kP = 0;
        config.slot0.kI = 0;
        config.slot0.kD = 0;
        config.slot0.kF = 0;

        // Select the motor's integrated encoder
        config.primaryPID.selectedFeedbackSensor = FeedbackDevice.IntegratedSensor;

        TalonFX fx = new TalonFX(canID, canBus);
        fx.configAllSettings(config);
        return fx;
    }

    public TalonFXMotor(int canID) {
        this(canID, "");
    }

    public TalonFXMotor(int canID, String canBus) {
        super(createFX(canID, canBus), 2048);
    }
}
