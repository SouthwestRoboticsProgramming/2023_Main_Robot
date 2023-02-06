package com.swrobotics.mathlib;

import java.util.Objects;

/**
 * Represents a two-dimensional vector of {@code double}s.
 */
public final class Vec2d {
    public double x;
    public double y;

    /**
     * Creates a new instance with both x and y set to zero.
     */
    public Vec2d() {
        x = 0;
        y = 0;
    }

    /**
     * Creates a new instance with given x and y components.
     *
     * @param x x component
     * @param y y component
     */
    public Vec2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a new instance with the same components as another vector.
     *
     * @param o other vector
     */
    public Vec2d(Vec2d o) {
        x = o.x;
        y = o.y;
    }

    /**
     * Creates a new instance in the direction of an angle
     * with a specified magnitude.
     *
     * @param angle direction angle
     * @param mag magnitude
     */
    public Vec2d(Angle angle, double mag) {
        x = angle.ccw().cos() * mag;
        y = angle.ccw().sin() * mag;
    }

    /**
     * Sets the X component of this vector.
     *
     * @param x x component
     * @return this
     */
    public Vec2d setX(double x) {
        this.x = x;
        return this;
    }

    /**
     * Sets the Y component of this vector.
     *
     * @param y y component
     * @return this
     */
    public Vec2d setY(double y) {
        this.y = y;
        return this;
    }

    /**
     * Sets both the X and Y components of this vector.
     *
     * @param x x component
     * @param y y component
     * @return this
     */
    public Vec2d set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Copies the X and Y components from another vector into this vector.
     *
     * @param o other vector
     * @return this
     */
    public Vec2d set(Vec2d o) {
        x = o.x;
        y = o.y;
        return this;
    }

    /**
     * Adds a vector with the specified components to this vector.
     *
     * @param x x component to add
     * @param y y component to add
     * @return this
     */
    public Vec2d add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     * Adds a vector with the specified components to this vector, and
     * stores the result in another vector.
     *
     * @param x x component to add
     * @param y y component to add
     * @param dest destination vector
     * @return dest
     */
    public Vec2d add(double x, double y, Vec2d dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        return dest;
    }

    /**
     * Adds another vector to this vector.
     *
     * @param o vector to add
     * @return this
     */
    public Vec2d add(Vec2d o) {
        x += o.x;
        y += o.y;
        return this;
    }

    /**
     * Adds another vector to this vector, and stores the result in
     * another vector.
     *
     * @param o vector to add
     * @param dest destination vector
     * @return dest
     */
    public Vec2d add(Vec2d o, Vec2d dest) {
        dest.x = x + o.x;
        dest.y = y + o.y;
        return dest;
    }

    /**
     * Subtracts a vector with the given components from this vector.
     *
     * @param x x component to subtract
     * @param y y component to subtract
     * @return this
     */
    public Vec2d sub(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    /**
     * Subtracts a vector with the given components from this vector,
     * and stores the result in another vector.
     *
     * @param x x component to subtract
     * @param y y component to subtract
     * @param dest destination vector
     * @return dest
     */
    public Vec2d sub(double x, double y, Vec2d dest) {
        dest.x = this.x - x;
        dest.y = this.y - y;
        return dest;
    }

    /**
     * Subtracts a vector from this vector.
     *
     * @param o vector to subtract
     * @return this
     */
    public Vec2d sub(Vec2d o) {
        x -= o.x;
        y -= o.y;
        return this;
    }

    /**
     * Subtracts a vector from this vector and stores the result in
     * another vector.
     *
     * @param o vector to subtract
     * @param dest destination vector
     * @return dest
     */
    public Vec2d sub(Vec2d o, Vec2d dest) {
        dest.x = x - o.x;
        dest.y = y - o.y;
        return dest;
    }

    /**
     * Multiplies this vector by a given scalar.
     *
     * @param scalar scalar to multiply by
     * @return this
     */
    public Vec2d mul(double scalar) {
        x *= scalar;
        y *= scalar;
        return this;
    }

    /**
     * Multiplies this vector by a given scalar and stores the result
     * in another vector.
     *
     * @param scalar scalar to multiply by
     * @param dest destination vector
     * @return dest
     */
    public Vec2d mul(double scalar, Vec2d dest) {
        dest.x = x * scalar;
        dest.y = y * scalar;
        return dest;
    }

    /**
     * Multiplies this vector by a vector with the given components.
     *
     * @param x x component to multiply by
     * @param y y component to multiply by
     * @return this
     */
    public Vec2d mul(double x, double y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    /**
     * Multiplies this vector by a vector with the given components, and
     * stores the result in another vector.
     *
     * @param x x component to multiply by
     * @param y y component to multiply by
     * @param dest destination vector
     * @return dest
     */
    public Vec2d mul(double x, double y, Vec2d dest) {
        dest.x = this.x * x;
        dest.y = this.y * y;
        return dest;
    }

    /**
     * Multiplies this vector by another vector.
     *
     * @param o vector to multiply by
     * @return this
     */
    public Vec2d mul(Vec2d o) {
        x *= o.x;
        y *= o.y;
        return this;
    }

    /**
     * Multiplies this vector by another vector and stores the result
     * in another vector.
     *
     * @param o vector to multiply by
     * @param dest destination vector
     * @return dest
     */
    public Vec2d mul(Vec2d o, Vec2d dest) {
        dest.x = x * o.x;
        dest.y = y * o.y;
        return dest;
    }

    /**
     * Divides this vector by a given scalar.
     *
     * @param scalar scalar to divide by
     * @return this
     */
    public Vec2d div(double scalar) {
        x /= scalar;
        y /= scalar;
        return this;
    }

    /**
     * Divides this vector by a given scalar and stores the result in
     * another vector.
     *
     * @param scalar scalar to divide by
     * @param dest destination vector
     * @return dest
     */
    public Vec2d div(double scalar, Vec2d dest) {
        dest.x = x / scalar;
        dest.y = y / scalar;
        return dest;
    }

    /**
     * Divides this vector by a vector with the given components.
     *
     * @param x x component to divide by
     * @param y y component to divide by
     * @return this
     */
    public Vec2d div(double x, double y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    /**
     * Divides this vector by a vector with the given components, and
     * stores the result in another vector.
     *
     * @param x x component to divide by
     * @param y y component to divide by
     * @param dest destination vector
     * @return dest
     */
    public Vec2d div(double x, double y, Vec2d dest) {
        dest.x = this.x / x;
        dest.y = this.y / y;
        return dest;
    }

    /**
     * Divides this vector by another vector.
     *
     * @param o vector to divide by
     * @return this
     */
    public Vec2d div(Vec2d o) {
        x /= o.x;
        y /= o.y;
        return this;
    }

    /**
     * Divides this vector by another vector, and stores the result
     * in another vector.
     *
     * @param o vector to divide by
     * @param dest destination vector
     * @return dest
     */
    public Vec2d div(Vec2d o, Vec2d dest) {
        dest.x = x / o.x;
        dest.y = y / o.y;
        return dest;
    }

    /**
     * Computes the magnitude of this vector, squared. This is more
     * efficient than computing the actual magnitude, so prefer this
     * function if you only need to compare the magnitudes of vectors.
     *
     * @return magnitude squared
     */
    public double magnitudeSq() {
        return x * x + y * y;
    }

    /**
     * Computes the magnitude of this vector.
     *
     * @return magnitude
     */
    public double magnitude() {
        return Math.sqrt(magnitudeSq());
    }

    /**
     * Computes the dot product between this vector and another vector.
     * This is equivalent to the cosine of the angle between them multiplied
     * by their magnitudes.
     *
     * @param o right hand side
     * @return dot product
     */
    public double dot(Vec2d o) {
        return x * o.x + y * o.y;
    }

    /**
     * Gets the angle this vector is facing in.
     *
     * @return angle
     */
    public Angle angle() {
        return CCWAngle.rad(Math.atan2(y, x));
    }

    /**
     * Gets the angle from this vector to another vector.
     *
     * @param o vector to compare with
     * @return angle between this vector and the other vector
     */
    public Angle angleTo(Vec2d o) {
        return angle().ccw().sub(o.angle().ccw()).abs();
    }

    /**
     * Computes the distance from this vector to a vector with the
     * specified components, squared. See the note in {@link #magnitudeSq()}
     * about efficiency.
     *
     * @param x x component of other vector
     * @param y y component of other vector
     * @return distance squared
     */
    public double distanceToSq(double x, double y) {
        double dx = this.x - x;
        double dy = this.y - y;
        return dx * dx + dy * dy;
    }

    /**
     * Computes the distance from this vector to another vector,
     * squared. See the note in {@link #magnitudeSq()} about efficiency.
     *
     * @param o other vector
     * @return distance squared
     */
    public double distanceToSq(Vec2d o) {
        double dx = x - o.x;
        double dy = y - o.y;
        return dx * dx + dy * dy;
    }

    /**
     * Computes the distance from this vector to a vector with the
     * specified components.
     *
     * @param x x component of other vector
     * @param y y component of other vector
     * @return distance
     */
    public double distanceTo(double x, double y) {
        return Math.sqrt(distanceToSq(x, y));
    }

    /**
     * Computes the distance from this vector to another vector.
     *
     * @param o other vector
     * @return distance
     */
    public double distanceTo(Vec2d o) {
        return Math.sqrt(distanceToSq(o));
    }

    public double distanceToLineSegmentSq(Vec2d a, Vec2d b) {
        double l2 = a.distanceToSq(b);
        if (l2 == 0) return distanceToSq(a);
        double t = ((x - a.x) * (b.x - a.x) + (y - a.y) * (b.y - a.y)) / l2;
        t = MathUtil.clamp(t, 0, 1);
        return distanceToSq(new Vec2d(MathUtil.lerp(a.x, b.x, t), MathUtil.lerp(a.y, b.y, t)));
    }

    public double distanceToLineSegment(Vec2d a, Vec2d b) {
        return Math.sqrt(distanceToLineSegmentSq(a, b));
    }

    /**
     * Rotates this vector by a given angle.
     *
     * @param angle angle to rotate by
     * @return this
     */
    public Vec2d rotateBy(Angle angle) {
        return rotateBy(angle, this);
    }

    /**
     * Rotates this vector by a given angle and stores the result
     * in another vector.
     *
     * @param angle angle to rotate by
     * @param dest destination vector
     * @return dest
     */
    public Vec2d rotateBy(Angle angle, Vec2d dest) {
        double sin = angle.ccw().sin();
        double cos = angle.ccw().cos();

        double nx = x * cos - y * sin;
        double ny = x * sin + y * cos;

        dest.x = nx;
        dest.y = ny;
        return dest;
    }

    /**
     * Normalizes this vector by making its magnitude 1, thus
     * becoming a unit vector.
     *
     * @return this
     */
    public Vec2d normalize() {
        double mag = magnitude();
        x /= mag;
        y /= mag;
        return this;
    }

    /**
     * Normalizes this vector by making its magnitude 1, and stores
     * the result in another vector.
     *
     * @param dest destination vector
     * @return dest
     */
    public Vec2d normalize(Vec2d dest) {
        double mag = magnitude();
        dest.x = x / mag;
        dest.y = y / mag;
        return dest;
    }

    /**
     * Negates this vector.
     *
     * @return this
     */
    public Vec2d negate() {
        x = -x;
        y = -y;
        return this;
    }

    /**
     * Negates this vector, and stores the result in another vector.
     *
     * @param dest destination vector
     * @return dest
     */
    public Vec2d negate(Vec2d dest) {
        dest.x = -x;
        dest.y = -y;
        return dest;
    }

    /**
     * Takes the absolute value of each component of this vector.
     *
     * @return this
     */
    public Vec2d absolute() {
        x = Math.abs(x);
        y = Math.abs(y);
        return this;
    }

    /**
     * Takes the absolute value of each component of this vector, and
     * stores the result in another vector.
     *
     * @param dest destination vector
     * @return dest
     */
    public Vec2d absolute(Vec2d dest) {
        dest.x = Math.abs(x);
        dest.y = Math.abs(y);
        return dest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec2d vec2d = (Vec2d) o;
        return Double.compare(vec2d.x, x) == 0 &&
                Double.compare(vec2d.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f)", x, y);
    }
}
