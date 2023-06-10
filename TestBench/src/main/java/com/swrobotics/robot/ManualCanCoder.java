package com.swrobotics.robot;

import com.swrobotics.lib.encoder.CanCoder;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.mathlib.CCWAngle;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public final class ManualCanCoder extends SubsystemBase {
    private final Encoder relative;
    private final Encoder absolute;

    private final NTEntry<Boolean> flipRelative;
    private final NTEntry<Boolean> flipAbsolute;

    private final NTEntry<Boolean> setRelative;
    private final NTEntry<Double> newRelative;

    private final NTEntry<Double> relativePos;
    private final NTEntry<Double> absolutePos;

    public ManualCanCoder(String root, int id) {
        CanCoder canCoder = new CanCoder(id);
        relative = canCoder.getRelative();
        absolute = canCoder.getAbsolute();

        flipRelative = new NTBoolean(root + "/relative/flip", false).setTemporary();
        flipAbsolute = new NTBoolean(root + "/absolute/flip", false).setTemporary();
        flipRelative.nowAndOnChange(() -> relative.setInverted(flipRelative.get()));
        flipAbsolute.nowAndOnChange(() -> absolute.setInverted(flipAbsolute.get()));

        setRelative = new NTBoolean(root + "/relative/set pos", false).setTemporary();
        newRelative = new NTDouble(root + "/relative/new pos", 0).setTemporary();

        relativePos = new NTDouble(root + "/relative/pos ccw", 0).setTemporary();
        absolutePos = new NTDouble(root + "/absolute/pos ccw", 0).setTemporary();
    }

    @Override
    public void periodic() {
        if (setRelative.get()) {
            setRelative.set(false);
            relative.setAngle(CCWAngle.rot(newRelative.get()));
        }

        relativePos.set(relative.getAngle().ccw().rot());
        absolutePos.set(absolute.getAngle().ccw().rot());
    }
}
