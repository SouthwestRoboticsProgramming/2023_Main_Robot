package com.swrobotics.lib.motor;

import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.encoder.SimEncoder;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.MathUtil;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

// FIXME: Do a proper simulation and fully implement
//        This is currently assuming no motor load with no inertia and missing PID control
public final class SimMotor extends SubsystemBase implements FeedbackMotor {
    public static final class MotorCaps {
        public final Angle freeSpeed;

        public MotorCaps(Angle freeSpeed) {
            this.freeSpeed = freeSpeed;
        }
    }

    public static final MotorCaps TALON_FX = new MotorCaps(CCWAngle.rad(Units.rotationsPerMinuteToRadiansPerSecond(6380)));
    public static final MotorCaps NEO = new MotorCaps(CCWAngle.rad(Units.rotationsPerMinuteToRadiansPerSecond(5676)));
    public static final MotorCaps NEO550 = new MotorCaps(CCWAngle.rad(Units.rotationsPerMinuteToRadiansPerSecond(11000)));

    private final Angle freeSpeed;
    private final SimEncoder integratedEncoder;
    private Angle rawAngle;

    private double flip;
    private double percentOut;

    public SimMotor(MotorCaps caps) {
        if (!RobotBase.isSimulation()) {
            DriverStation.reportError("SimMotor used on real robot!", true);
        }

        this.freeSpeed = caps.freeSpeed;
        rawAngle = Angle.ZERO;
        flip = 1;
        integratedEncoder = new SimEncoder(() -> rawAngle.mul(flip));
        percentOut = 0;
    }

    @Override
    public void periodic() {
        rawAngle = rawAngle.add(freeSpeed.mul(MathUtil.clamp(percentOut, -1, 1) * flip * 0.02));
    }

    @Override
    public void setPercentOut(double percent) {
        percentOut = percent;
    }

    @Override
    public void setPosition(Angle position) {
        throw new AssertionError("Not yet implemented");
    }

    @Override
    public void setVelocity(Angle velocity) {
        throw new AssertionError("Not yet implemented");
    }

    /**
     * @return integrated encoder. Guaranteed to be a SimEncoder
     */
    @Override
    public Encoder getIntegratedEncoder() {
        return integratedEncoder;
    }

    @Override
    public void setIntegratedEncoder(Encoder encoder) {
        throw new AssertionError("Not yet implemented");
    }

    @Override
    public void setInverted(boolean inverted) {
        flip = inverted ? -1 : 1;
    }

    @Override
    public void follow(Motor leader) {
        throw new AssertionError("Not yet implemented");
    }

    @Override
    public void resetIntegrator() {
        throw new AssertionError("Not yet implemented");
    }

    @Override
    public void setP(double kP) {
        throw new AssertionError("Not yet implemented");
    }

    @Override
    public void setI(double kI) {
        throw new AssertionError("Not yet implemented");
    }

    @Override
    public void setD(double kD) {
        throw new AssertionError("Not yet implemented");
    }

    @Override
    public void setF(double kF) {
        throw new AssertionError("Not yet implemented");
    }
}
