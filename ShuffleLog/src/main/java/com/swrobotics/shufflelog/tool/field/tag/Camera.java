package com.swrobotics.shufflelog.tool.field.tag;

import com.swrobotics.shufflelog.math.Matrix4f;
import com.swrobotics.shufflelog.tool.field.GizmoTarget;

public final class Camera implements GizmoTarget {
    private final Matrix4f referencePose;
    private final String name;
    private final int port;
    private Matrix4f transform;

    public Camera(String name, int port, Matrix4f transform, Matrix4f ref) {
        referencePose = ref;
        this.name = name;
        this.port = port;
        this.transform = transform;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public Matrix4f getRawTransform() {
        return transform;
    }

    @Override
    public Matrix4f getTransform() {
        return referencePose.mul(transform, new Matrix4f());
    }

    @Override
    public void setTransform(Matrix4f transform) {
        this.transform = referencePose.invert(new Matrix4f()).mul(transform);
    }
}
