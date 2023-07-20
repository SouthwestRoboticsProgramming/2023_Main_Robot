package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.lib.encoder.CanCoder;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.encoder.SimEncoder;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.motor.SimMotor;
import com.swrobotics.lib.motor.rev.NEOMotor;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.lib.net.NTPrimitive;
import com.swrobotics.mathlib.Angle;
import edu.wpi.first.wpilibj.RobotBase;

// Note: "Horizontal" as a reference point here refers to true horizontal for
//       the two segments and facing forward parallel to the top segment for the wrist
public class ArmJoint {
    protected final FeedbackMotor motor;
    protected final Encoder motorEncoder;
    private final Encoder absoluteEncoder;

    private final double canCoderToArmRatio;
    protected final double motorToArmRatio;
    private final NTEntry<Angle> absEncoderOffset;

    /**
     * Creates a new arm joint
     *
     * @param motorId CAN id of the motor Spark MAX
     * @param canCoderId CAN id of the CanCoder
     * @param canCoderToArmRatio gear reduction from the CanCoder axis to the arm axis
     * @param motorToArmRatio gear reduction from the motor shaft to the arm axis
     * @param absEncoderOffset NTAngle to store CanCoder offset into
     * @param invert whether to invert output. This should be true if a ccw rotation of the
     *               motor output shaft corresponds to cw rotation of the arm
     */
    public ArmJoint(int motorId, int canCoderId, double canCoderToArmRatio, double motorToArmRatio, NTEntry<Angle> absEncoderOffset, boolean invert) {
        if (RobotBase.isReal()) {
            motor = new NEOMotor(motorId);
            motorEncoder = motor.getIntegratedEncoder();
            absoluteEncoder = new CanCoder(canCoderId).getAbsolute();
        } else {
            motor = new SimMotor(SimMotor.NEO);
            motorEncoder = motor.getIntegratedEncoder();
            absoluteEncoder = new SimEncoder(() ->
                    ((SimEncoder) motorEncoder).getRawAngle().mul(canCoderToArmRatio / motorToArmRatio)
            );
        }
        this.absEncoderOffset = absEncoderOffset;

        this.canCoderToArmRatio = canCoderToArmRatio;
        this.motorToArmRatio = motorToArmRatio;

        motor.setInverted(invert);
        absoluteEncoder.setInverted(invert);
    }

    public void setBrakeMode(boolean brake) {
        motor.setBrakeMode(brake);
    }

    /**
     * Gets the current angle of this joint relative to horizontal.
     *
     * @return current angle
     */
    public Angle getCurrentAngle() {
        return motorEncoder.getAngle().div(motorToArmRatio);
    }

    protected Angle getCalibrationAngle(Angle home) {
        // Get CanCoder position: angle relative to home angle at the cancoder axis
        Angle canCoderPos = absoluteEncoder.getAngle().add(absEncoderOffset.get());

        // Arm position: position of arm relative to horizontal
        return home.add(canCoderPos.div(canCoderToArmRatio));
    }

    /**
     * Calibrates the motor encoder using the absolute encoder's reading. This is
     * called on startup so the arm can start in any position.
     */
    public void calibratePosition(Angle home) {
        motorEncoder.setAngle(getCalibrationAngle(home).mul(motorToArmRatio));
    }

    /**
     * Calibrates the CanCoder to the home position. This assumes the arm is
     * currently at the home angle physically.
     */
    public void calibrateCanCoder() {
        // Set offset such that the current position is considered 0
        absEncoderOffset.set(absoluteEncoder.getAngle().negate());
    }

    /**
     * Sets the motor percent output for this joint. Positive percent corresponds to CCW angle from
     * {@link #getCurrentAngle()}.
     */
    public void setMotorOutput(double percent) {
        motor.setPercentOut(percent);
    }
}
