package com.swrobotics.lib.encoder;

import com.swrobotics.mathlib.Angle;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.function.Supplier;

public final class SimEncoder extends SubsystemBase implements Encoder {
    private final Supplier<Angle> rawAngleSupplier;

    private Angle rawAngle, rawVelocity;
    private Angle offset;
    private double flip;

    public SimEncoder(Supplier<Angle> rawAngleSupplier) {
        if (!RobotBase.isSimulation()) {
            DriverStation.reportError("SimEncoder used on real robot!", true);
        }

        this.rawAngleSupplier = rawAngleSupplier;

        rawAngle = rawAngleSupplier.get();
        offset = Angle.ZERO;
        flip = 1;
    }

    @Override
    public void periodic() {
        Angle prevAngle = rawAngle;
        rawAngle = rawAngleSupplier.get().add(offset);
        rawVelocity = rawAngle.sub(prevAngle).mul(0.02);
    }

    public Angle getRawAngle() {
        return rawAngle;
    }

    public Angle getRawVelocity() {
        return rawVelocity;
    }

    @Override
    public Angle getAngle() {
        return rawAngle.mul(flip);
    }

    @Override
    public Angle getVelocity() {
        return rawVelocity.mul(flip);
    }

    @Override
    public void setAngle(Angle angle) {
        offset = angle.mul(flip).sub(this.rawAngle);
    }

    @Override
    public void setInverted(boolean inverted) {
        flip = inverted ? -1 : 1;
    }
}
