package com.swrobotics.lib.net;

import edu.wpi.first.math.controller.PIDController;

public final class NTUtil {
    public static PIDController tunablePID(NTEntry<Double> kP, NTEntry<Double> kI, NTEntry<Double> kD) {
        PIDController pid = new PIDController(kP.get(), kI.get(), kD.get());
        kP.onChange(pid::setP);
        kI.onChange(pid::setI);
        kD.onChange(pid::setD);
        return pid;
    }
}
