package com.swrobotics.robot.subsystems.arm.joint;

import com.swrobotics.mathlib.MathUtil;
import edu.wpi.first.math.util.Units;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.RevoluteJoint;

public final class SimJoint implements ArmJoint {
    private static final double FREE_SPEED = Units.rotationsPerMinuteToRadiansPerSecond(5676);

    private final Body body;
    private final RevoluteJoint joint;
    private final double gearRatio;

    public SimJoint(Body body, RevoluteJoint joint, double gearRatio) {
        this.body = body;
        this.joint = joint;
        this.gearRatio = gearRatio;
    }

    @Override
    public double getCurrentAngle() {
        return body.getAngle();
    }

    @Override
    public void setCurrentAngle(double angle) {
        body.setTransform(body.getPosition(), (float) angle);
    }

    @Override
    public void setMotorOutput(double motor) {
        // This is not how motors work but it's good enough for testing
        joint.setMotorSpeed((float) (MathUtil.clamp(motor, -1, 1) * FREE_SPEED / gearRatio));
    }
}
