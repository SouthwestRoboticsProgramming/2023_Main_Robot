package com.swrobotics.robot.subsystems.arm2;

import com.swrobotics.lib.encoder.CanCoder;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.motor.rev.NEOMotor;
import com.swrobotics.lib.net.NTAngle;
import com.swrobotics.mathlib.Angle;

// Note: "Horizontal" as a reference point here refers to true horizontal for
//       the two segments and facing forward parallel to the top segment for the wrist
// TODO: Figure out what zero should mean for the wrist - it should be relative to the
//       top segment, but what is the "direction" of the intake?
public final class ArmJoint {
    private final FeedbackMotor motor;
    private final Encoder motorEncoder;
    private final Encoder absoluteEncoder;

    private final double canCoderToArmRatio;
    private final double motorToArmRatio;
    private final NTAngle absEncoderOffset;

    /**
     * Creates a new arm joint
     *
     * @param motorId CAN id of the motor Spark MAX
     * @param canCoderId CAN id of the CanCoder
     * @param absEncoderOffset NTAngle to store CanCoder offset into
     * @param invert whether to invert output. This should be true if a ccw rotation of the
     *               motor output shaft corresponds to cw rotation of the arm
     */
    public ArmJoint(int motorId, int canCoderId, double canCoderToArmRatio, double motorToArmRatio, NTAngle absEncoderOffset, boolean invert) {
        motor = new NEOMotor(motorId);
        motorEncoder = motor.getIntegratedEncoder();
        absoluteEncoder = new CanCoder(canCoderId).getAbsolute();
        this.absEncoderOffset = absEncoderOffset;

        this.canCoderToArmRatio = canCoderToArmRatio;
        this.motorToArmRatio = motorToArmRatio;

        motor.setInverted(invert);
        absoluteEncoder.setInverted(invert);
    }

    /**
     * Gets the current angle of this joint relative to horizontal.
     *
     * @return current angle
     */
    public Angle getCurrentAngle() {
        return motorEncoder.getAngle().ccw().div(motorToArmRatio);
    }

    /**
     * Calibrates the motor encoder using the absolute encoder's reading. This is
     * called on startup so the arm can start in any position.
     */
    public void calibratePosition(Angle home) {
        // Get CanCoder position: angle relative to home angle at the cancoder axis
        Angle canCoderPos = absoluteEncoder.getAngle().ccw().sub(absEncoderOffset.get().ccw());

        // Arm position: position of arm relative to horizontal
        Angle armPos = home.ccw().add(canCoderPos.ccw().div(canCoderToArmRatio));

        motorEncoder.setAngle(armPos.ccw().mul(motorToArmRatio));
    }

    /**
     * Calibrates the CanCoder to the home position. This assumes the arm is
     * currently at the home angle physically.
     */
    public void calibrateCanCoder() {
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
