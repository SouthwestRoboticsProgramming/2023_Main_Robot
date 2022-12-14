package com.swrobotics.shufflelog.util;

import com.swrobotics.shufflelog.math.Matrix4f;
import processing.core.PMatrix;

public final class ProcessingUtils {
    public static void setPMatrix(PMatrix dst, Matrix4f src) {
        dst.set(
                src.m00, src.m01, src.m02, src.m03,
                src.m10, src.m11, src.m12, src.m13,
                src.m20, src.m21, src.m22, src.m23,
                src.m30, src.m31, src.m32, src.m33
        );
    }

    private ProcessingUtils() {
        throw new AssertionError();
    }
}
