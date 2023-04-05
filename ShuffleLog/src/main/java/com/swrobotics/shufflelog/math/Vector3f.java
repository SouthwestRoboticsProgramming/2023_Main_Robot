package com.swrobotics.shufflelog.math;

import java.nio.ByteBuffer;

public final class Vector3f {
    public float x;
    public float y;
    public float z;

    public Vector3f() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f(Vector2f xy, float z) {
        x = xy.x;
        y = xy.y;
        this.z = z;
    }

    public Vector3f(float x, Vector2f yz) {
        this.x = x;
        y = yz.x;
        z = yz.y;
    }

    public Vector3f(Vector3f vec) {
        x = vec.x;
        y = vec.y;
        z = vec.z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public Vector3f setX(float x) {
        this.x = x;
        return this;
    }

    public Vector3f setY(float y) {
        this.y = y;
        return this;
    }

    public Vector3f setZ(float z) {
        this.z = z;
        return this;
    }

    public Vector3f set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3f set(Vector2f xy, float z) {
        x = xy.x;
        y = xy.y;
        this.z = z;
        return this;
    }

    public Vector3f set(float x, Vector2f yz) {
        this.x = x;
        y = yz.x;
        z = yz.y;
        return this;
    }

    public Vector3f set(Vector3f vec) {
        x = vec.x;
        y = vec.y;
        z = vec.z;
        return this;
    }

    public Vector3f put(ByteBuffer buf) {
        buf.putFloat(x);
        buf.putFloat(y);
        buf.putFloat(z);
        return this;
    }

    public Vector3f add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector3f add(float x, float y, float z, Vector3f dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        dest.z = this.z + z;
        return dest;
    }

    public Vector3f add(Vector3f vec) {
        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }

    public Vector3f add(Vector3f vec, Vector3f dest) {
        dest.x = x + vec.x;
        dest.y = y + vec.y;
        dest.z = z + vec.z;
        return dest;
    }

    public Vector3f sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vector3f sub(float x, float y, float z, Vector3f dest) {
        dest.x = this.x - x;
        dest.y = this.y - y;
        dest.z = this.z - z;
        return dest;
    }

    public Vector3f sub(Vector3f vec) {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        return this;
    }

    public Vector3f sub(Vector3f vec, Vector3f dest) {
        dest.x = x - vec.x;
        dest.y = y - vec.y;
        dest.z = z - vec.z;
        return dest;
    }

    public Vector3f mul(float f) {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }

    public Vector3f mul(float x, float y, float z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vector3f mul(float x, float y, float z, Vector3f dest) {
        dest.x = this.x * x;
        dest.y = this.y * y;
        dest.z = this.z * z;
        return dest;
    }

    public Vector3f mul(Vector3f vec) {
        x *= vec.x;
        y *= vec.y;
        z *= vec.z;
        return this;
    }

    public Vector3f mul(Vector3f vec, Vector3f dest) {
        dest.x = x * vec.x;
        dest.y = y * vec.y;
        dest.z = z * vec.z;
        return dest;
    }

    public Vector3f mulAdd(Vector3f vec, float scalar) {
        x += vec.x * scalar;
        y += vec.y * scalar;
        z *= vec.z * scalar;
        return this;
    }

    public Vector3f div(float f) {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }

    public Vector3f div(float x, float y, float z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    public Vector3f div(float x, float y, float z, Vector3f dest) {
        dest.x = this.x / x;
        dest.y = this.y / y;
        dest.z = this.z / z;
        return dest;
    }

    public Vector3f div(Vector3f vec) {
        x /= vec.x;
        y /= vec.y;
        z /= vec.z;
        return this;
    }

    public Vector3f div(Vector3f vec, Vector3f dest) {
        dest.x = x / vec.x;
        dest.y = y / vec.y;
        dest.z = z / vec.z;
        return dest;
    }

    public Vector3f rotateX(float angle) {
        float s = MathUtils.sinf(angle);
        float c = MathUtils.cosf(angle);

        float ny = y * c - z * s;
        float nz = y * s + z * c;
        y = ny;
        z = nz;

        return this;
    }

    public Vector3f rotateY(float angle) {
        float s = MathUtils.sinf(angle);
        float c = MathUtils.cosf(angle);

        float nx = x * c + z * s;
        float nz = -x * s + z * c;
        x = nx;
        z = nz;

        return this;
    }

    public Vector3f rotateZ(float angle) {
        float s = MathUtils.sinf(angle);
        float c = MathUtils.cosf(angle);

        float nx = x * c - y * s;
        float ny = x * s + y * c;
        x = nx;
        y = ny;

        return this;
    }

    public float length() {
        return MathUtils.sqrtf(x * x + y * y + z * z);
    }

    public float lengthSquared() {
        return x * x + y * y + z * z;
    }

    public static float distance(float x1, float y1, float z1, float x2, float y2, float z2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float dz = z2 - z2;
        return MathUtils.sqrtf(dx * dx + dy * dy + dz * dz);
    }

    public static float distanceSquared(
            float x1, float y1, float z1, float x2, float y2, float z2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float dz = z1 - z2;
        return dx * dx + dy * dy + dz * dz;
    }

    public float distance(float x, float y, float z) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        return MathUtils.sqrtf(dx * dx + dy * dy + dz * dz);
    }

    public float distance(Vector3f vec) {
        float dx = x - vec.x;
        float dy = y - vec.y;
        float dz = z - vec.z;
        return MathUtils.sqrtf(dx * dx + dy * dy + dz * dz);
    }

    public float distanceSquared(float x, float y, float z) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public float distanceSquared(Vector3f vec) {
        float dx = x - vec.x;
        float dy = y - vec.y;
        float dz = z - vec.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public Vector3f normalize() {
        float len = length();
        x /= len;
        y /= len;
        z /= len;
        return this;
    }

    public Vector3f normalize(Vector3f dest) {
        float len = length();
        dest.x = x / len;
        dest.y = y / len;
        dest.z = z / len;
        return dest;
    }

    public Vector3f absolute() {
        x = Math.abs(x);
        y = Math.abs(y);
        z = Math.abs(z);
        return this;
    }

    public Vector3f absolute(Vector3f dest) {
        dest.x = Math.abs(x);
        dest.y = Math.abs(y);
        dest.z = Math.abs(z);
        return dest;
    }

    public Vector3f cross(Vector3f rhs) {
        float nx = y * rhs.z - z * rhs.y;
        float ny = z * rhs.x - x * rhs.z;
        float nz = x * rhs.y - y * rhs.x;
        x = nx;
        y = ny;
        z = nz;
        return this;
    }

    public Vector3f cross(Vector3f rhs, Vector3f dest) {
        dest.x = y * rhs.z - z * rhs.y;
        dest.y = z * rhs.x - x * rhs.z;
        dest.z = x * rhs.y - y * rhs.x;
        return dest;
    }

    public float dot(Vector3f o) {
        return x * o.x + y * o.y + z * o.z;
    }

    public Vector3f negate() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public Vector3f negate(Vector3f dest) {
        dest.x = -x;
        dest.y = -y;
        dest.z = -z;
        return dest;
    }

    public Vector3f zero() {
        x = 0;
        y = 0;
        z = 0;
        return this;
    }
}
