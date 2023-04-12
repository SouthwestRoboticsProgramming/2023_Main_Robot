package com.swrobotics.shufflelog.math;

import java.nio.ByteBuffer;

public final class Vector2f {
    public float x;
    public float y;

    public Vector2f() {
        x = 0;
        y = 0;
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f(Vector2f vec) {
        x = vec.x;
        y = vec.y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getComponent(int index) {
        switch (index) {
            case 0:
                return x;
            case 1:
                return y;
            default:
                throw new IndexOutOfBoundsException(String.valueOf(index));
        }
    }

    public Vector2f setX(float x) {
        this.x = x;
        return this;
    }

    public Vector2f setY(float y) {
        this.y = y;
        return this;
    }

    public Vector2f set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2f set(Vector2f vec) {
        x = vec.x;
        y = vec.y;
        return this;
    }

    public Vector2f put(ByteBuffer buf) {
        buf.putFloat(x);
        buf.putFloat(y);
        return this;
    }

    public Vector2f add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2f add(float x, float y, Vector2f dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        return dest;
    }

    public Vector2f add(Vector2f vec) {
        x += vec.x;
        y += vec.y;
        return this;
    }

    public Vector2f add(Vector2f vec, Vector2f dest) {
        dest.x = x + vec.x;
        dest.y = y + vec.y;
        return dest;
    }

    public Vector2f sub(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vector2f sub(float x, float y, Vector2f dest) {
        dest.x = this.x - x;
        dest.y = this.y - y;
        return dest;
    }

    public Vector2f sub(Vector2f vec) {
        x -= vec.x;
        y -= vec.y;
        return this;
    }

    public Vector2f sub(Vector2f vec, Vector2f dest) {
        dest.x = x - vec.x;
        dest.y = y - vec.y;
        return dest;
    }

    public Vector2f mul(float f) {
        x *= f;
        y *= f;
        return this;
    }

    public Vector2f mul(float f, Vector2f dest) {
        dest.x = x * f;
        dest.y = y * f;
        return dest;
    }

    public Vector2f mul(float x, float y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vector2f mul(float x, float y, Vector2f dest) {
        dest.x = this.x * x;
        dest.y = this.y * y;
        return dest;
    }

    public Vector2f mul(Vector2f vec) {
        x *= vec.x;
        y *= vec.y;
        return this;
    }

    public Vector2f mul(Vector2f vec, Vector2f dest) {
        dest.x = x * vec.x;
        dest.y = y * vec.y;
        return dest;
    }

    public Vector2f div(float f) {
        x /= f;
        y /= f;
        return this;
    }

    public Vector2f div(float x, float y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public Vector2f div(float x, float y, Vector2f dest) {
        dest.x = this.x / x;
        dest.y = this.y / y;
        return dest;
    }

    public Vector2f div(Vector2f vec) {
        x /= vec.x;
        y /= vec.y;
        return this;
    }

    public Vector2f div(Vector2f vec, Vector2f dest) {
        dest.x = x / vec.x;
        dest.y = y / vec.y;
        return dest;
    }

    public float length() {
        return MathUtils.sqrtf(x * x + y * y);
    }

    public float lengthSquared() {
        return x * x + y * y;
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return MathUtils.sqrtf(dx * dx + dy * dy);
    }

    public static float distanceSquared(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    public float distance(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return MathUtils.sqrtf(dx * dx + dy * dy);
    }

    public float distance(Vector2f vec) {
        float dx = x - vec.x;
        float dy = y - vec.y;
        return MathUtils.sqrtf(dx * dx + dy * dy);
    }

    public float distanceSquared(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return dx * dx + dy * dy;
    }

    public float distanceSquared(Vector2f vec) {
        float dx = x - vec.x;
        float dy = y - vec.y;
        return dx * dx + dy * dy;
    }

    public Vector2f normalize() {
        float len = length();
        x /= len;
        y /= len;
        return this;
    }

    public Vector2f normalize(Vector2f dest) {
        float len = length();
        dest.x = x / len;
        dest.y = y / len;
        return dest;
    }

    public Vector2f absolute() {
        x = Math.abs(x);
        y = Math.abs(y);
        return this;
    }

    public Vector2f absolute(Vector2f dest) {
        dest.x = Math.abs(x);
        dest.y = Math.abs(y);
        return dest;
    }

    public Vector2f negate() {
        x = -x;
        y = -y;
        return this;
    }

    public Vector2f negate(Vector2f dest) {
        dest.x = -x;
        dest.y = -y;
        return dest;
    }

    public Vector2f zero() {
        x = 0;
        y = 0;
        return this;
    }

    public Vector2f max(float x, float y) {
        this.x = Math.max(x, this.x);
        this.y = Math.max(y, this.y);
        return this;
    }

    public Vector2f max(float x, float y, Vector2f dest) {
        dest.x = Math.max(x, this.x);
        dest.y = Math.max(y, this.y);
        return dest;
    }

    public Vector2f max(Vector2f v) {
        x = Math.max(v.x, x);
        y = Math.max(v.y, y);
        return this;
    }

    public Vector2f max(Vector2f v, Vector2f dest) {
        dest.x = Math.max(v.x, x);
        dest.y = Math.max(v.y, y);
        return dest;
    }

    @Override
    public String toString() {
        return String.format("(%3.3f, %3.3f)", x, y);
    }
}
