package com.swrobotics.shufflelog.math;

public final class Ray3f {
    public Vector3f origin;
    public Vector3f direction;

    public Ray3f(Vector3f origin, Vector3f direction) {
        this.origin = new Vector3f(origin);
        this.direction = new Vector3f(direction).normalize();
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public void setOrigin(Vector3f origin) {
        this.origin = origin;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "Ray3f{" + "origin=" + origin + ", direction=" + direction + '}';
    }
}
