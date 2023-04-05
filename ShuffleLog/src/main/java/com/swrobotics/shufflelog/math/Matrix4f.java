package com.swrobotics.shufflelog.math;

public final class Matrix4f {
    public float m00, m01, m02, m03;
    public float m10, m11, m12, m13;
    public float m20, m21, m22, m23;
    public float m30, m31, m32, m33;

    public static Matrix4f fromColumnMajor(float[] m) {
        return new Matrix4f(
                m[0], m[4], m[8], m[12], m[1], m[5], m[9], m[13], m[2], m[6], m[10], m[14], m[3],
                m[7], m[11], m[15]);
    }

    public Matrix4f() {
        identity();
    }

    public Matrix4f(Matrix4f m) {
        m00 = m.m00;
        m01 = m.m01;
        m02 = m.m02;
        m03 = m.m03;
        m10 = m.m10;
        m11 = m.m11;
        m12 = m.m12;
        m13 = m.m13;
        m20 = m.m20;
        m21 = m.m21;
        m22 = m.m22;
        m23 = m.m23;
        m30 = m.m30;
        m31 = m.m31;
        m32 = m.m32;
        m33 = m.m33;
    }

    public Matrix4f(
            float m00,
            float m01,
            float m02,
            float m03,
            float m10,
            float m11,
            float m12,
            float m13,
            float m20,
            float m21,
            float m22,
            float m23,
            float m30,
            float m31,
            float m32,
            float m33) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    public Matrix4f identity() {
        m00 = 1;
        m01 = 0;
        m02 = 0;
        m03 = 0;
        m10 = 0;
        m11 = 1;
        m12 = 0;
        m13 = 0;
        m20 = 0;
        m21 = 0;
        m22 = 1;
        m23 = 0;
        m30 = 0;
        m31 = 0;
        m32 = 0;
        m33 = 1;
        return this;
    }

    public Matrix4f ortho(float left, float right, float bottom, float top, float near, float far) {
        identity();

        float width = right - left;
        float height = top - bottom;
        float depth = far - near;

        m00 = 2 / width;
        m11 = 2 / height;
        m22 = 2 / depth;
        m03 = -(right + left) / width;
        m13 = -(top + bottom) / height;
        m23 = -(far + near) / depth;

        return this;
    }

    public Matrix4f perspective(float fov, float aspect, float near, float far) {
        identity();

        float tanHalfFOV = MathUtils.tanf(fov / 2);
        m00 = 1 / (aspect * tanHalfFOV);
        m11 = 1 / tanHalfFOV;
        m22 = -(far + near) / (far - near);
        m32 = -1;
        m23 = -(2 * far * near) / (far - near);
        m33 = 0;

        return this;
    }

    public Matrix4f translate(Vector3f vec) {
        return mul(1, 0, 0, vec.x, 0, 1, 0, vec.y, 0, 0, 1, vec.z, 0, 0, 0, 1);
    }

    public Matrix4f rotateX(float angle) {
        float s = MathUtils.sinf(angle);
        float c = MathUtils.cosf(angle);

        return mul(1, 0, 0, 0, 0, c, -s, 0, 0, s, c, 0, 0, 0, 0, 1);
    }

    public Matrix4f rotateY(float angle) {
        float s = MathUtils.sinf(angle);
        float c = MathUtils.cosf(angle);

        return mul(c, 0, s, 0, 0, 1, 0, 0, -s, 0, c, 0, 0, 0, 0, 1);
    }

    public Matrix4f rotateZ(float angle) {
        float s = MathUtils.sinf(angle);
        float c = MathUtils.cosf(angle);

        return mul(c, -s, 0, 0, s, c, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
    }

    public Matrix4f scale(Vector3f vec) {
        return mul(vec.x, 0, 0, 0, 0, vec.y, 0, 0, 0, 0, vec.z, 0, 0, 0, 0, 1);
    }

    public Matrix4f mul(Matrix4f m) {
        return mul(
                m.m00, m.m01, m.m02, m.m03, m.m10, m.m11, m.m12, m.m13, m.m20, m.m21, m.m22, m.m23,
                m.m30, m.m31, m.m32, m.m33);
    }

    public Matrix4f mul(Matrix4f m, Matrix4f dest) {
        return mul(
                m.m00, m.m01, m.m02, m.m03, m.m10, m.m11, m.m12, m.m13, m.m20, m.m21, m.m22, m.m23,
                m.m30, m.m31, m.m32, m.m33, dest);
    }

    public Matrix4f mul(
            float m00,
            float m01,
            float m02,
            float m03,
            float m10,
            float m11,
            float m12,
            float m13,
            float m20,
            float m21,
            float m22,
            float m23,
            float m30,
            float m31,
            float m32,
            float m33) {
        float n00 = this.m00 * m00 + this.m01 * m10 + this.m02 * m20 + this.m03 * m30;
        float n01 = this.m00 * m01 + this.m01 * m11 + this.m02 * m21 + this.m03 * m31;
        float n02 = this.m00 * m02 + this.m01 * m12 + this.m02 * m22 + this.m03 * m32;
        float n03 = this.m00 * m03 + this.m01 * m13 + this.m02 * m23 + this.m03 * m33;
        float n10 = this.m10 * m00 + this.m11 * m10 + this.m12 * m20 + this.m13 * m30;
        float n11 = this.m10 * m01 + this.m11 * m11 + this.m12 * m21 + this.m13 * m31;
        float n12 = this.m10 * m02 + this.m11 * m12 + this.m12 * m22 + this.m13 * m32;
        float n13 = this.m10 * m03 + this.m11 * m13 + this.m12 * m23 + this.m13 * m33;
        float n20 = this.m20 * m00 + this.m21 * m10 + this.m22 * m20 + this.m23 * m30;
        float n21 = this.m20 * m01 + this.m21 * m11 + this.m22 * m21 + this.m23 * m31;
        float n22 = this.m20 * m02 + this.m21 * m12 + this.m22 * m22 + this.m23 * m32;
        float n23 = this.m20 * m03 + this.m21 * m13 + this.m22 * m23 + this.m23 * m33;
        float n30 = this.m30 * m00 + this.m31 * m10 + this.m32 * m20 + this.m33 * m30;
        float n31 = this.m30 * m01 + this.m31 * m11 + this.m32 * m21 + this.m33 * m31;
        float n32 = this.m30 * m02 + this.m31 * m12 + this.m32 * m22 + this.m33 * m32;
        float n33 = this.m30 * m03 + this.m31 * m13 + this.m32 * m23 + this.m33 * m33;
        this.m00 = n00;
        this.m01 = n01;
        this.m02 = n02;
        this.m03 = n03;
        this.m10 = n10;
        this.m11 = n11;
        this.m12 = n12;
        this.m13 = n13;
        this.m20 = n20;
        this.m21 = n21;
        this.m22 = n22;
        this.m23 = n23;
        this.m30 = n30;
        this.m31 = n31;
        this.m32 = n32;
        this.m33 = n33;
        return this;
    }

    public Matrix4f mul(
            float m00,
            float m01,
            float m02,
            float m03,
            float m10,
            float m11,
            float m12,
            float m13,
            float m20,
            float m21,
            float m22,
            float m23,
            float m30,
            float m31,
            float m32,
            float m33,
            Matrix4f dest) {
        dest.m00 = this.m00 * m00 + this.m01 * m10 + this.m02 * m20 + this.m03 * m30;
        dest.m01 = this.m00 * m01 + this.m01 * m11 + this.m02 * m21 + this.m03 * m31;
        dest.m02 = this.m00 * m02 + this.m01 * m12 + this.m02 * m22 + this.m03 * m32;
        dest.m03 = this.m00 * m03 + this.m01 * m13 + this.m02 * m23 + this.m03 * m33;
        dest.m10 = this.m10 * m00 + this.m11 * m10 + this.m12 * m20 + this.m13 * m30;
        dest.m11 = this.m10 * m01 + this.m11 * m11 + this.m12 * m21 + this.m13 * m31;
        dest.m12 = this.m10 * m02 + this.m11 * m12 + this.m12 * m22 + this.m13 * m32;
        dest.m13 = this.m10 * m03 + this.m11 * m13 + this.m12 * m23 + this.m13 * m33;
        dest.m20 = this.m20 * m00 + this.m21 * m10 + this.m22 * m20 + this.m23 * m30;
        dest.m21 = this.m20 * m01 + this.m21 * m11 + this.m22 * m21 + this.m23 * m31;
        dest.m22 = this.m20 * m02 + this.m21 * m12 + this.m22 * m22 + this.m23 * m32;
        dest.m23 = this.m20 * m03 + this.m21 * m13 + this.m22 * m23 + this.m23 * m33;
        dest.m30 = this.m30 * m00 + this.m31 * m10 + this.m32 * m20 + this.m33 * m30;
        dest.m31 = this.m30 * m01 + this.m31 * m11 + this.m32 * m21 + this.m33 * m31;
        dest.m32 = this.m30 * m02 + this.m31 * m12 + this.m32 * m22 + this.m33 * m32;
        dest.m33 = this.m30 * m03 + this.m31 * m13 + this.m32 * m23 + this.m33 * m33;
        return dest;
    }

    public Vector4f mul(Vector4f v) {
        return new Vector4f(
                m00 * v.x + m01 * v.y + m02 * v.z + m03 * v.w,
                m10 * v.x + m11 * v.y + m12 * v.z + m13 * v.w,
                m20 * v.x + m21 * v.y + m22 * v.z + m23 * v.w,
                m30 * v.x + m31 * v.y + m32 * v.z + m33 * v.w);
    }

    public Vector3f transformPosition(Vector3f v) {
        float x = m00 * v.x + m01 * v.y + m02 * v.z + m03;
        float y = m10 * v.x + m11 * v.y + m12 * v.z + m13;
        float z = m20 * v.x + m21 * v.y + m22 * v.z + m23;
        return new Vector3f(x, y, z);
    }

    public Vector3f transformDirection(Vector3f v) {
        float x = m00 * v.x + m01 * v.y + m02 * v.z;
        float y = m10 * v.x + m11 * v.y + m12 * v.z;
        float z = m20 * v.x + m21 * v.y + m22 * v.z;
        return new Vector3f(x, y, z);
    }

    public Matrix4f invert() {
        return invert(this);
    }

    // From https://stackoverflow.com/a/44446912
    public Matrix4f invert(Matrix4f dest) {
        float a2323 = m22 * m33 - m23 * m32;
        float a1323 = m21 * m33 - m23 * m31;
        float a1223 = m21 * m32 - m22 * m31;
        float a0323 = m20 * m33 - m23 * m30;
        float a0223 = m20 * m32 - m22 * m30;
        float a0123 = m20 * m31 - m21 * m30;
        float a2313 = m12 * m33 - m13 * m32;
        float a1313 = m11 * m33 - m13 * m31;
        float a1213 = m11 * m32 - m12 * m31;
        float a2312 = m12 * m23 - m13 * m22;
        float a1312 = m11 * m23 - m13 * m21;
        float a1212 = m11 * m22 - m12 * m21;
        float a0313 = m10 * m33 - m13 * m30;
        float a0213 = m10 * m32 - m12 * m30;
        float a0312 = m10 * m23 - m13 * m20;
        float a0212 = m10 * m22 - m12 * m20;
        float a0113 = m10 * m31 - m11 * m30;
        float a0112 = m10 * m21 - m11 * m20;

        float det =
                m00 * (m11 * a2323 - m12 * a1323 + m13 * a1223)
                        - m01 * (m10 * a2323 - m12 * a0323 + m13 * a0223)
                        + m02 * (m10 * a1323 - m11 * a0323 + m13 * a0123)
                        - m03 * (m10 * a1223 - m11 * a0223 + m12 * a0123);
        det = 1 / det;

        float n00 = det * (m11 * a2323 - m12 * a1323 + m13 * a1223);
        float n01 = det * -(m01 * a2323 - m02 * a1323 + m03 * a1223);
        float n02 = det * (m01 * a2313 - m02 * a1313 + m03 * a1213);
        float n03 = det * -(m01 * a2312 - m02 * a1312 + m03 * a1212);
        float n10 = det * -(m10 * a2323 - m12 * a0323 + m13 * a0223);
        float n11 = det * (m00 * a2323 - m02 * a0323 + m03 * a0223);
        float n12 = det * -(m00 * a2313 - m02 * a0313 + m03 * a0213);
        float n13 = det * (m00 * a2312 - m02 * a0312 + m03 * a0212);
        float n20 = det * (m10 * a1323 - m11 * a0323 + m13 * a0123);
        float n21 = det * -(m00 * a1323 - m01 * a0323 + m03 * a0123);
        float n22 = det * (m00 * a1313 - m01 * a0313 + m03 * a0113);
        float n23 = det * -(m00 * a1312 - m01 * a0312 + m03 * a0112);
        float n30 = det * -(m10 * a1223 - m11 * a0223 + m12 * a0123);
        float n31 = det * (m00 * a1223 - m01 * a0223 + m02 * a0123);
        float n32 = det * -(m00 * a1213 - m01 * a0213 + m02 * a0113);
        float n33 = det * (m00 * a1212 - m01 * a0212 + m02 * a0112);

        dest.m00 = n00;
        dest.m01 = n01;
        dest.m02 = n02;
        dest.m03 = n03;
        dest.m10 = n10;
        dest.m11 = n11;
        dest.m12 = n12;
        dest.m13 = n13;
        dest.m20 = n20;
        dest.m21 = n21;
        dest.m22 = n22;
        dest.m23 = n23;
        dest.m30 = n30;
        dest.m31 = n31;
        dest.m32 = n32;
        dest.m33 = n33;

        return dest;
    }

    public float getM00() {
        return m00;
    }

    public float getM01() {
        return m01;
    }

    public float getM02() {
        return m02;
    }

    public float getM03() {
        return m03;
    }

    public float getM10() {
        return m10;
    }

    public float getM11() {
        return m11;
    }

    public float getM12() {
        return m12;
    }

    public float getM13() {
        return m13;
    }

    public float getM20() {
        return m20;
    }

    public float getM21() {
        return m21;
    }

    public float getM22() {
        return m22;
    }

    public float getM23() {
        return m23;
    }

    public float getM30() {
        return m30;
    }

    public float getM31() {
        return m31;
    }

    public float getM32() {
        return m32;
    }

    public float getM33() {
        return m33;
    }

    public float get(int i, int j) {
        switch (i) {
            case 0:
                switch (j) {
                    case 0:
                        return m00;
                    case 1:
                        return m01;
                    case 2:
                        return m02;
                    case 3:
                        return m03;
                }
            case 1:
                switch (j) {
                    case 0:
                        return m10;
                    case 1:
                        return m11;
                    case 2:
                        return m12;
                    case 3:
                        return m13;
                }
            case 2:
                switch (j) {
                    case 0:
                        return m20;
                    case 1:
                        return m21;
                    case 2:
                        return m22;
                    case 3:
                        return m23;
                }
            case 3:
                switch (j) {
                    case 0:
                        return m30;
                    case 1:
                        return m31;
                    case 2:
                        return m32;
                    case 3:
                        return m33;
                }
        }

        throw new IndexOutOfBoundsException(i + ", " + j);
    }

    public Matrix4f setM00(float m00) {
        this.m00 = m00;
        return this;
    }

    public Matrix4f setM01(float m01) {
        this.m01 = m01;
        return this;
    }

    public Matrix4f setM02(float m02) {
        this.m02 = m02;
        return this;
    }

    public Matrix4f setM03(float m03) {
        this.m03 = m03;
        return this;
    }

    public Matrix4f setM10(float m10) {
        this.m10 = m10;
        return this;
    }

    public Matrix4f setM11(float m11) {
        this.m11 = m11;
        return this;
    }

    public Matrix4f setM12(float m12) {
        this.m12 = m12;
        return this;
    }

    public Matrix4f setM13(float m13) {
        this.m13 = m13;
        return this;
    }

    public Matrix4f setM20(float m20) {
        this.m20 = m20;
        return this;
    }

    public Matrix4f setM21(float m21) {
        this.m21 = m21;
        return this;
    }

    public Matrix4f setM22(float m22) {
        this.m22 = m22;
        return this;
    }

    public Matrix4f setM23(float m23) {
        this.m23 = m23;
        return this;
    }

    public Matrix4f setM30(float m30) {
        this.m30 = m30;
        return this;
    }

    public Matrix4f setM31(float m31) {
        this.m31 = m31;
        return this;
    }

    public Matrix4f setM32(float m32) {
        this.m32 = m32;
        return this;
    }

    public Matrix4f setM33(float m33) {
        this.m33 = m33;
        return this;
    }

    public Matrix4f set(int i, int j, float m) {
        switch (i) {
            case 0:
                switch (j) {
                    case 0:
                        m00 = m;
                        break;
                    case 1:
                        m01 = m;
                        break;
                    case 2:
                        m02 = m;
                        break;
                    case 3:
                        m03 = m;
                        break;
                    default:
                        throw new IndexOutOfBoundsException(i + ", " + j);
                }
                break;
            case 1:
                switch (j) {
                    case 0:
                        m10 = m;
                        break;
                    case 1:
                        m11 = m;
                        break;
                    case 2:
                        m12 = m;
                        break;
                    case 3:
                        m13 = m;
                        break;
                    default:
                        throw new IndexOutOfBoundsException(i + ", " + j);
                }
                break;
            case 2:
                switch (j) {
                    case 0:
                        m20 = m;
                        break;
                    case 1:
                        m21 = m;
                        break;
                    case 2:
                        m22 = m;
                        break;
                    case 3:
                        m23 = m;
                        break;
                    default:
                        throw new IndexOutOfBoundsException(i + ", " + j);
                }
                break;
            case 3:
                switch (j) {
                    case 0:
                        m30 = m;
                        break;
                    case 1:
                        m31 = m;
                        break;
                    case 2:
                        m32 = m;
                        break;
                    case 3:
                        m33 = m;
                        break;
                    default:
                        throw new IndexOutOfBoundsException(i + ", " + j);
                }
                break;
            default:
                throw new IndexOutOfBoundsException(i + ", " + j);
        }
        return this;
    }

    public float[] getRowMajor() {
        return new float[] {
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33
        };
    }

    public float[] getColumnMajor() {
        return new float[] {
            m00, m10, m20, m30,
            m01, m11, m21, m31,
            m02, m12, m22, m32,
            m03, m13, m23, m33
        };
    }

    public Matrix4f set(
            float m00,
            float m01,
            float m02,
            float m03,
            float m10,
            float m11,
            float m12,
            float m13,
            float m20,
            float m21,
            float m22,
            float m23,
            float m30,
            float m31,
            float m32,
            float m33) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        return this;
    }

    public Matrix4f setFromColumnMajor(float[] m) {
        return set(
                m[0], m[4], m[8], m[12], m[1], m[5], m[9], m[13], m[2], m[6], m[10], m[14], m[3],
                m[7], m[11], m[15]);
    }

    @Override
    public String toString() {
        return "[\n"
                + String.format("%6.3f %6.3f %6.3f %6.3f\n", m00, m01, m02, m03)
                + String.format("%6.3f %6.3f %6.3f %6.3f\n", m10, m11, m12, m13)
                + String.format("%6.3f %6.3f %6.3f %6.3f\n", m20, m21, m22, m23)
                + String.format("%6.3f %6.3f %6.3f %6.3f\n", m30, m31, m32, m33)
                + "]";
    }
}
