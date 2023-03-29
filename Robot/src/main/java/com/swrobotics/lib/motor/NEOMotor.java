package com.swrobotics.lib.motor;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CWAngle;
import edu.wpi.first.math.util.Units;

public final class NEOMotor implements FeedbackMotor {
    private final CANSparkMax spark;
    private final SparkMaxPIDController pid;
    private final Encoder integratedEncoder;

    private boolean inverted;
    private CANSparkMax leader;

    public NEOMotor(int canID) {
        spark = new CANSparkMax(canID, CANSparkMaxLowLevel.MotorType.kBrushless);
        pid = spark.getPIDController();

        RelativeEncoder encoder = spark.getEncoder();
        encoder.setPositionConversionFactor(1); // Rotations
        encoder.setVelocityConversionFactor(1); // RPM
        pid.setFeedbackDevice(encoder);

        inverted = false;
        leader = null;

        integratedEncoder = new Encoder() {
            @Override
            public Angle getAngle() {
                return CWAngle.rot(encoder.getPosition());
            }

            @Override
            public Angle getVelocity() {
                return CWAngle.rad(Units.rotationsPerMinuteToRadiansPerSecond(encoder.getVelocity()));
            }

            @Override
            public void setAngle(Angle angle) {
                encoder.setPosition(angle.cw().rot());
            }
        };
    }

    @Override
    public void setPercentOut(double percent) {
        pid.setReference(percent, CANSparkMax.ControlType.kDutyCycle);
    }

    @Override
    public void setPosition(Angle position) {
        pid.setReference(position.cw().rot(), CANSparkMax.ControlType.kPosition);
    }

    @Override
    public void setVelocity(Angle velocity) {
        pid.setReference(
                Units.radiansPerSecondToRotationsPerMinute(velocity.cw().rad()),
                CANSparkMax.ControlType.kVelocity
        );
    }

    @Override
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        if (leader != null) {
            spark.follow(leader, inverted);
        } else {
            spark.setInverted(inverted);
        }
    }

    @Override
    public void follow(Motor leader) {
        if (!(leader instanceof NEOMotor))
            throw new UnsupportedOperationException("NEO motors can only follow other NEO motors");
        if (this.leader != null)
            throw new IllegalStateException("Already following a motor");

        CANSparkMax leaderSpark = ((NEOMotor) leader).spark;
        spark.follow(leaderSpark, inverted);

        this.leader = leaderSpark;
    }

    @Override
    public void setBrakeMode(boolean brake) {
        spark.setIdleMode(brake ? CANSparkMax.IdleMode.kBrake : CANSparkMax.IdleMode.kCoast);
    }

    @Override
    public Encoder getIntegratedEncoder() {
        return integratedEncoder;
    }

    @Override
    public void resetIntegrator() {
        pid.setIAccum(0);
    }

    @Override
    public void setP(double kP) {
        pid.setP(kP);
    }

    @Override
    public void setI(double kI) {
        pid.setI(kI);
    }

    @Override
    public void setD(double kD) {
        pid.setD(kD);
    }

    @Override
    public void setF(double kF) {
        pid.setFF(kF);
    }
}
