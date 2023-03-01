package com.swrobotics.robot.subsystems.arnold;

import com.swrobotics.lib.net.NTDouble;
import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Arnold extends SubsystemBase {
    public enum ArnoldState {
        IN,
        OUT,
        STOP,
    }

    private ArnoldState state = ArnoldState.STOP;
    private NTDouble Speed = new NTDouble("ARNOLD/SPEED", 0.75);

    private final PWMTalonSRX left_side;
    private final PWMTalonSRX right_side;

    public Arnold(int left_channel, int right_channel) {
        left_side = new PWMTalonSRX(left_channel);
        right_side = new PWMTalonSRX(right_channel);
        left_side.setInverted(false);
        right_side.setInverted(true);

    }

    @Override
    public void periodic() {
        switch (state) {
            case IN:
                left_side.set(-Speed.get());
                right_side.set(-Speed.get());
                break;
            case OUT:
                right_side.set(Speed.get());
                left_side.set(Speed.get());
                break;
            case STOP:
                left_side.set(0);
                right_side.set(0);
                break;
        }
    }

    public void setState(ArnoldState state) {
        this.state = state;
    }

    public boolean isSpinning() {
        return (state == ArnoldState.IN || state == ArnoldState.OUT);
    }
}
