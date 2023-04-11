package com.swrobotics.shufflelog.tool.field;

import com.swrobotics.shufflelog.math.Matrix4f;

public interface GizmoTarget {
    Matrix4f getTransform();

    void setTransform(Matrix4f transform);
}
