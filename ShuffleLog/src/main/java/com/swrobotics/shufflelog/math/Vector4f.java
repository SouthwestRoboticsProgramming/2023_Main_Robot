package com.swrobotics.shufflelog.math;

import java.nio.ByteBuffer;

public final class Vector4f {
    public float x;
    public float y;
    public float z;
    public float w;

    public Vector4f() {
        x = 0;
        y = 0;
        z = 0;
        w = 0;
    }

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4f(Vector2f xy, float z, float w) {
        x = xy.x;
        y = xy.y;
        this.z = z;
        this.w = w;
    }

    public Vector4f(float x, Vector2f yz, float w) {
        this.x = x;
        y = yz.x;
        z = yz.y;
        this.w = w;
    }

    public Vector4f(float x, float y, Vector2f zw) {
        this.x = x;
        this.y = y;
        z = zw.x;
        w = zw.y;
    }

    public Vector4f(Vector2f xy, Vector2f zw) {
        x = xy.x;
        y = xy.y;
        z = zw.x;
        w = zw.y;
    }

    public Vector4f(Vector3f xyz, float w) {
        x = xyz.x;
        y = xyz.y;
        z = xyz.z;
        this.w = w;
    }

    public Vector4f(float x, Vector3f yzw) {
        this.x = x;
        y = yzw.x;
        z = yzw.y;
        w = yzw.z;
    }

    public Vector4f(Vector4f vec) {
        x = vec.x;
        y = vec.y;
        z = vec.z;
        w = vec.w;
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

    public float getW() {
        return w;
    }

    public Vector4f setX(float x) {
        this.x = x;
        return this;
    }

    public Vector4f setY(float y) {
        this.y = y;
        return this;
    }

    public Vector4f setZ(float z) {
        this.z = z;
        return this;
    }

    public Vector4f setW(float w) {
        this.w = w;
        return this;
    }

    public Vector4f set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vector4f set(Vector2f xy, float z, float w) {
        x = xy.x;
        y = xy.y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vector4f set(float x, Vector2f yz, float w) {
        this.x = x;
        y = yz.x;
        z = yz.y;
        this.w = w;
        return this;
    }

    public Vector4f set(float x, float y, Vector2f zw) {
        this.x = x;
        this.y = y;
        z = zw.x;
        w = zw.y;
        return this;
    }

    public Vector4f set(Vector2f xy, Vector2f zw) {
        x = xy.x;
        y = xy.y;
        z = zw.x;
        w = zw.y;
        return this;
    }

    public Vector4f set(Vector3f xyz, float w) {
        x = xyz.x;
        y = xyz.y;
        z = xyz.z;
        this.w = w;
        return this;
    }

    public Vector4f set(float x, Vector3f yzw) {
        this.x = x;
        y = yzw.x;
        z = yzw.y;
        w = yzw.z;
        return this;
    }

    public Vector4f set(Vector4f vec) {
        x = vec.x;
        y = vec.y;
        z = vec.z;
        w = vec.w;
        return this;
    }

    public Vector4f put(ByteBuffer buf) {
        buf.putFloat(x);
        buf.putFloat(y);
        buf.putFloat(z);
        buf.putFloat(w);
        return this;
    }

    public Vector4f add(float x, float y, float z, float w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Vector4f add(float x, float y, float z, float w, Vector4f dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        dest.z = this.z + z;
        dest.w = this.w + w;
        return dest;
    }

    public Vector4f add(Vector4f vec) {
        x += vec.x;
        y += vec.y;
        z += vec.z;
        w += vec.w;
        return this;
    }

    public Vector4f add(Vector4f vec, Vector4f dest) {
        dest.x = x + vec.x;
        dest.y = y + vec.y;
        dest.z = z + vec.z;
        dest.w = w + vec.w;
        return dest;
    }

    public Vector4f sub(float x, float y, float z, float w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    public Vector4f sub(float x, float y, float z, float w, Vector4f dest) {
        dest.x = this.x - x;
        dest.y = this.y - y;
        dest.z = this.z - z;
        dest.w = this.w - w;
        return dest;
    }

    public Vector4f sub(Vector4f vec) {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        w -= vec.w;
        return this;
    }

    public Vector4f sub(Vector4f vec, Vector4f dest) {
        dest.x = x - vec.x;
        dest.y = y - vec.y;
        dest.z = z - vec.z;
        dest.w = w - vec.w;
        return dest;
    }

    public Vector4f mul(float x, float y, float z, float w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public Vector4f mul(float x, float y, float z, float w, Vector4f dest) {
        dest.x = this.x * x;
        dest.y = this.y * y;
        dest.z = this.z * z;
        dest.w = this.w * w;
        return dest;
    }

    public Vector4f mul(float f) {
        x *= f;
        y *= f;
        z *= f;
        w *= f;
        return this;
    }

    public Vector4f mul(float f, Vector4f dest) {
        dest.x = x * f;
        dest.y = y * f;
        dest.z = z * f;
        dest.w = w * f;
        return dest;
    }

    public Vector4f mul(Vector4f vec) {
        x *= vec.x;
        y *= vec.y;
        z *= vec.z;
        w *= vec.w;
        return this;
    }

    public Vector4f mul(Vector4f vec, Vector4f dest) {
        dest.x = x * vec.x;
        dest.y = y * vec.y;
        dest.z = z * vec.z;
        dest.w = w * vec.w;
        return dest;
    }

    public Vector4f div(float x, float y, float z, float w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        return this;
    }

    public Vector4f div(float x, float y, float z, float w, Vector4f dest) {
        dest.x = this.x / x;
        dest.y = this.y / y;
        dest.z = this.z / z;
        dest.w = this.w / w;
        return dest;
    }

    public Vector4f div(Vector4f vec) {
        x /= vec.x;
        y /= vec.y;
        z /= vec.z;
        w /= vec.w;
        return this;
    }

    public Vector4f div(Vector4f vec, Vector4f dest) {
        dest.x = x / vec.x;
        dest.y = y / vec.y;
        dest.z = z / vec.z;
        dest.w = w / vec.w;
        return dest;
    }

    public float length() {
        return MathUtils.sqrtf(x * x + y * y + z * z + w * w);
    }

    public float lengthSquared() {
        return x * x + y * y + z * z + w * w;
    }

    public static float distance(
            float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2) {
        float dx = x1 - y2;
        float dy = y1 - y2;
        float dz = z1 - z2;
        float dw = w1 - w2;
        return MathUtils.sqrtf(dx * dx + dy * dy + dz * dz + dw * dw);
    }

    public static float distanceSquared(
            float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float dz = z1 - z2;
        float dw = w1 - w2;
        return dx * dx + dy * dy + dz * dz + dw * dw;
    }

    public float distance(float x, float y, float z, float w) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        float dw = this.w - w;
        return MathUtils.sqrtf(dx * dx + dy * dy + dz * dz + dw * dw);
    }

    public float distance(Vector4f vec) {
        float dx = x - vec.x;
        float dy = y - vec.y;
        float dz = z - vec.z;
        float dw = w - vec.w;
        return MathUtils.sqrtf(dx * dx + dy * dy + dz * dz + dw * dw);
    }

    public float distanceSquared(float x, float y, float z, float w) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        float dw = this.w - w;
        return dx * dx * dy * dy + dz * dz + dw * dw;
    }

    public float distanceSquared(Vector4f vec) {
        float dx = x - vec.x;
        float dy = y - vec.y;
        float dz = z - vec.z;
        float dw = w - vec.w;
        return dx * dx + dy * dy + dz * dz + dw * dw;
    }

    public Vector4f normalize() {
        float len = length();
        x /= len;
        y /= len;
        z /= len;
        w /= len;
        return this;
    }

    public Vector4f normalize(Vector4f dest) {
        float len = length();
        dest.x = x / len;
        dest.y = y / len;
        dest.z = z / len;
        dest.w = w / len;
        return dest;
    }

    public Vector4f absolute() {
        x = Math.abs(x);
        y = Math.abs(y);
        z = Math.abs(z);
        w = Math.abs(w);
        return this;
    }

    public Vector4f absolute(Vector4f dest) {
        dest.x = Math.abs(x);
        dest.y = Math.abs(y);
        dest.z = Math.abs(z);
        dest.w = Math.abs(w);
        return dest;
    }

    public Vector4f negate() {
        x = -x;
        y = -y;
        z = -z;
        w = -w;
        return this;
    }

    public Vector4f negate(Vector4f dest) {
        dest.x = -x;
        dest.y = -y;
        dest.z = -z;
        dest.w = -w;
        return dest;
    }

    public Vector4f zero() {
        x = 0;
        y = 0;
        z = 0;
        w = 0;
        return this;
    }
}
