package com.swrobotics.robot.subsystems.intake2;

import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTDouble;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Intake2 extends SubsystemBase {

    public NTBoolean hasElement = new NTBoolean("INTAKE/HASELEMENT", false);
    public static final int MOTOR_PORT = 0;
    public static final int BEAM_SENSOR_ONE_PORT = 1;
    public static final int BEAM_SENSOR_TWO_PORT = 2;

    private PWMSparkMax motor;

    private final DigitalInput beamBreakOne;
    private final DigitalInput beamBreakTwo;

    private static final NTDouble MOTOR_SPEED = new NTDouble("INTAKE/SPEED", 0.25);

    public final NTDouble CUBE_INTAKE_CONTINUE = new NTDouble("INTAKE/CUBE_INTAKE_CONTINUE", 0.25);
    public final NTDouble CONE_INTAKE_CONTINUE = new NTDouble("INTAKE/CONE_INTAKE_CONTINUE", 0.25);


    public Intake2() {
        motor = new PWMSparkMax(MOTOR_PORT);
        beamBreakOne = new DigitalInput(BEAM_SENSOR_ONE_PORT);
        beamBreakTwo = new DigitalInput(BEAM_SENSOR_TWO_PORT);
    }

    public boolean cubeBeamIsBroken() {
        return !beamBreakOne.get();
    }

    public boolean coneBeamIsBroken() {
        return !beamBreakTwo.get();
    }

    public void Outake() {
        motor.set(-MOTOR_SPEED.get());
    }

    public void Intake() {
        motor.set(MOTOR_SPEED.get());
    }

    public void Stop() {
        motor.set(0);
    }



}
