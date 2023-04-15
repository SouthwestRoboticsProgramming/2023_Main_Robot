package com.swrobotics.shufflelog.tool.field.tag;

import com.swrobotics.shufflelog.math.Matrix4f;
import com.swrobotics.shufflelog.math.Vector3f;

public final class RobotPose {
    private final Vector3f size;
    private final Matrix4f transform;

    public RobotPose(Vector3f size, Matrix4f transform) {
        this.size = size;
        this.transform = transform;
    }

    public Vector3f getSize() {
        return size;
    }

    public Matrix4f getTransform() {
        return transform;
    }
}
