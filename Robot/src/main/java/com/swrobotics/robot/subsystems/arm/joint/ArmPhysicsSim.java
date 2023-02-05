package com.swrobotics.robot.subsystems.arm.joint;

import com.swrobotics.robot.subsystems.arm.ArmSubsystem;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

public final class ArmPhysicsSim {
    private final World physicsWorld;
    private final SimJoint bottomJoint, topJoint;

    private Body createBody(double x, double w) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.position = new Vec2((float) x, 0);
        Body body = physicsWorld.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((float) w / 2, 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1;
        body.createFixture(fixtureDef);

        return body;
    }

    public ArmPhysicsSim() {
        float bottomLen = (float) ArmSubsystem.BOTTOM_LENGTH;
        float topLen = (float) ArmSubsystem.TOP_LENGTH;

        physicsWorld = new World(new Vec2(0, -1));

        BodyDef anchorDef = new BodyDef();
        anchorDef.type = BodyType.STATIC;
        anchorDef.position = new Vec2(0, 0);
        Body anchor = physicsWorld.createBody(anchorDef);

        Body bottom = createBody(bottomLen / 2, bottomLen);
        Body top = createBody(bottomLen + topLen / 2, topLen);

        RevoluteJointDef anchorToBottomDef = new RevoluteJointDef();
        anchorToBottomDef.bodyA = anchor;
        anchorToBottomDef.bodyB = bottom;
        anchorToBottomDef.localAnchorA = new Vec2(0, 0);
        anchorToBottomDef.localAnchorB = new Vec2(-bottomLen / 2, 0);
        anchorToBottomDef.enableMotor = true;
        anchorToBottomDef.maxMotorTorque = 2f * (float) ArmSubsystem.BOTTOM_GEAR_RATIO;
        anchorToBottomDef.motorSpeed = 0;
        RevoluteJoint bottomJoint = (RevoluteJoint) physicsWorld.createJoint(anchorToBottomDef);

        RevoluteJointDef bottomToTopDef = new RevoluteJointDef();
        bottomToTopDef.bodyA = bottom;
        bottomToTopDef.bodyB = top;
        bottomToTopDef.localAnchorA = new Vec2(bottomLen / 2, 0);
        bottomToTopDef.localAnchorB = new Vec2(-topLen / 2, 0);
        bottomToTopDef.enableMotor = true;
        bottomToTopDef.maxMotorTorque = 2f * (float) ArmSubsystem.TOP_GEAR_RATIO;
        bottomToTopDef.motorSpeed = 0;
        RevoluteJoint topJoint = (RevoluteJoint) physicsWorld.createJoint(bottomToTopDef);

        this.bottomJoint = new SimJoint(bottom, bottomJoint, ArmSubsystem.BOTTOM_GEAR_RATIO);
        this.topJoint = new SimJoint(top, topJoint, ArmSubsystem.TOP_GEAR_RATIO);
    }

    public void update() {
        physicsWorld.step(0.02f, 6, 2);
    }

    public SimJoint getBottomJoint() {
        return bottomJoint;
    }

    public SimJoint getTopJoint() {
        return topJoint;
    }
}
