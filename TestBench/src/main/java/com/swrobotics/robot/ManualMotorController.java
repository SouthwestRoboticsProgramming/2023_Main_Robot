package com.swrobotics.robot;

import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.motor.Motor;
import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.lib.net.NTEnum;
import com.swrobotics.mathlib.CCWAngle;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public final class ManualMotorController extends SubsystemBase {
    private enum ControlMode {
        PERCENT,
        POSITION,
        VELOCITY
    }

    private final NTEntry<Boolean> enabled;
    private final NTEntry<ControlMode> controlMode;
    private final NTEntry<Double> demand;
    private final NTEntry<Boolean> inverted;

    private final NTEntry<Double> encoderPos, encoderVel;
    private final NTEntry<Boolean> setEncoderPos;
    private final NTEntry<Double> newEncoderPos;

    private final Motor motor;
    private final Encoder encoder;

    public ManualMotorController(String root, Motor motor, boolean canSetPhase, boolean canSetPosition) {
        this.motor = motor;

        enabled = new NTBoolean(root + "/control/enable", false).setTemporary();
        demand = new NTDouble(root + "/control/demand", 0).setTemporary();
        inverted = new NTBoolean(root + "/control/inverted", false).setTemporary();
        inverted.nowAndOnChange(() -> motor.setInverted(inverted.get()));

        if (motor instanceof FeedbackMotor) {
            controlMode =
                    new NTEnum<>(root + "/control/mode", ControlMode.class, ControlMode.PERCENT)
                            .setTemporary();

            NTEntry<Double> kP = new NTDouble(root + "/pidf/kP", 0).setTemporary();
            NTEntry<Double> kI = new NTDouble(root + "/pidf/kI", 0).setTemporary();
            NTEntry<Double> kD = new NTDouble(root + "/pidf/kD", 0).setTemporary();
            NTEntry<Double> kF = new NTDouble(root + "/pidf/kF", 0).setTemporary();

            encoderPos = new NTDouble(root + "/encoder/position ccw", 0).setTemporary();
            encoderVel = new NTDouble(root + "/encoder/velocity ccw", 0).setTemporary();

            FeedbackMotor m = (FeedbackMotor) motor;
            m.setPIDF(kP, kI, kD, kF);
            encoder = m.getIntegratedEncoder();

            if (canSetPhase && encoder != null) {
                NTEntry<Boolean> sensorPhase = new NTBoolean(root + "/encoder/phase", false).setTemporary();
                sensorPhase.nowAndOnChange(() -> encoder.setInverted(sensorPhase.get()));
            }
            if (canSetPosition) {
                setEncoderPos = new NTBoolean(root + "/encoder/set position", false).setTemporary();
                newEncoderPos = new NTDouble(root + "/encoder/new position", 0).setTemporary();
            } else {
                setEncoderPos = null;
                newEncoderPos = null;
            }
        } else {
            controlMode = null;
            encoderPos = encoderVel = null;
            encoder = null;
            setEncoderPos = null;
            newEncoderPos = null;
        }
    }

    @Override
    public void periodic() {
        if (!DriverStation.isTeleop())
            return;

        if (!enabled.get()) {
            motor.stop();
            return;
        }

        if (motor instanceof FeedbackMotor) {
            FeedbackMotor m = (FeedbackMotor) motor;
            double demand = this.demand.get();
            switch (controlMode.get()) {
                case PERCENT: m.setPercentOut(demand); break;
                case POSITION: m.setPosition(CCWAngle.rot(demand)); break;
                case VELOCITY: m.setVelocity(CCWAngle.rot(demand)); break;
            }

            if (encoder != null) {
                if (setEncoderPos != null && setEncoderPos.get()) {
                    setEncoderPos.set(false);
                    encoder.setAngle(CCWAngle.rot(newEncoderPos.get()));
                }

                encoderPos.set(encoder.getAngle().ccw().rot());
                encoderVel.set(encoder.getVelocity().ccw().rot());
            }
        } else {
            motor.setPercentOut(demand.get());
        }
    }
}
