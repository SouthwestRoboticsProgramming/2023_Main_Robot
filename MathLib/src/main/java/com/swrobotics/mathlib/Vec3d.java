package com.swrobotics.mathlib;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;

import java.util.Objects;

/** Represents a three-dimensional vector of {@code double}s. */
public final class Vec3d {
    public double x;
    public double y;
    public double z;

    /** Creates a new instance with x, y, and z set to zero. */
    public Vec3d() {
        x = 0;
        y = 0;
        z = 0;
    }

    /**
     * Creates a new instance with given x, y, and z components.
     *
     * @param x x component
     * @param y y component
     * @param z z component
     */
    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a new instance with the same components as another vector.
     *
     * @param o other vector
     */
    public Vec3d(Vec3d o) {
        x = o.x;
        y = o.y;
        z = o.z;
    }

    /** Gets this vector as a Translation2d. */
    public Translation3d translation3d() {
        return new Translation3d(x, y, z);
    }

    /**
     * Sets the X component of this vector.
     *
     * @param x x component
     * @return this
     */
    public Vec3d setX(double x) {
        this.x = x;
        return this;
    }

    /**
     * Sets the Y component of this vector.
     *
     * @param y y component
     * @return this
     */
    public Vec3d setY(double y) {
        this.y = y;
        return this;
    }

    /**
     * Sets the Z component of this vector.
     *
     * @param z z component
     * @return this
     */
    public Vec3d setZ(double z) {
        this.z = z;
        return this;
    }

    /**
     * Sets the X, Y, and Z components of this vector.
     *
     * @param x x component
     * @param y y component
     * @param z z component
     * @return this
     */
    public Vec3d set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Copies the X and Y components from another vector into this vector.
     *
     * @param o other vector
     * @return this
     */
    public Vec3d set(Vec3d o) {
        x = o.x;
        y = o.y;
        z = o.z;
        return this;
    }

    /**
     * Adds a vector with the specified components to this vector.
     *
     * @param x x component to add
     * @param y y component to add
     * @param z z component to add
     * @return this
     */
    public Vec3d add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    /**
     * Adds a vector with the specified components to this vector, and stores the result in another
     * vector.
     *
     * @param x x component to add
     * @param y y component to add
     * @param z z component to add
     * @param dest destination vector
     * @return dest
     */
    public Vec3d add(double x, double y, double z, Vec3d dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        dest.z = this.z + z;
        return dest;
    }

    /**
     * Adds another vector to this vector.
     *
     * @param o vector to add
     * @return this
     */
    public Vec3d add(Vec3d o) {
        x += o.x;
        y += o.y;
        z += o.z;
        return this;
    }

    /**
     * Adds another vector to this vector, and stores the result in another vector.
     *
     * @param o vector to add
     * @param dest destination vector
     * @return dest
     */
    public Vec3d add(Vec3d o, Vec3d dest) {
        dest.x = x + o.x;
        dest.y = y + o.y;
        dest.z = z + o.z;
        return dest;
    }

    /**
     * Subtracts a vector with the given components from this vector.
     *
     * @param x x component to subtract
     * @param y y component to subtract
     * @param z z component to subtract
     * @return this
     */
    public Vec3d sub(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    /**
     * Subtracts a vector with the given components from this vector, and stores the result in
     * another vector.
     *
     * @param x x component to subtract
     * @param y y component to subtract
     * @param z z component to subtract
     * @param dest destination vector
     * @return dest
     */
    public Vec3d sub(double x, double y, double z, Vec3d dest) {
        dest.x = this.x - x;
        dest.y = this.y - y;
        dest.z = this.z - z;
        return dest;
    }

    /**
     * Subtracts a vector from this vector.
     *
     * @param o vector to subtract
     * @return this
     */
    public Vec3d sub(Vec3d o) {
        x -= o.x;
        y -= o.y;
        z -= o.z;
        return this;
    }

    /**
     * Subtracts a vector from this vector and stores the result in another vector.
     *
     * @param o vector to subtract
     * @param dest destination vector
     * @return dest
     */
    public Vec3d sub(Vec3d o, Vec3d dest) {
        dest.x = x - o.x;
        dest.y = y - o.y;
        dest.z = z - o.z;
        return dest;
    }

    /**
     * Multiplies this vector by a given scalar.
     *
     * @param scalar scalar to multiply by
     * @return this
     */
    public Vec3d mul(double scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }

    /**
     * Multiplies this vector by a given scalar and stores the result in another vector.
     *
     * @param scalar scalar to multiply by
     * @param dest destination vector
     * @return dest
     */
    public Vec3d mul(double scalar, Vec3d dest) {
        dest.x = x * scalar;
        dest.y = y * scalar;
        dest.z = z * scalar;
        return dest;
    }

    /**
     * Multiplies this vector by a vector with the given components.
     *
     * @param x x component to multiply by
     * @param y y component to multiply by
     * @param z z component to multiply by
     * @return this
     */
    public Vec3d mul(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    /**
     * Multiplies this vector by a vector with the given components, and stores the result in
     * another vector.
     *
     * @param x x component to multiply by
     * @param y y component to multiply by
     * @param z z component to multiply by
     * @param dest destination vector
     * @return dest
     */
    public Vec3d mul(double x, double y, double z, Vec3d dest) {
        dest.x = this.x * x;
        dest.y = this.y * y;
        dest.z = this.z * z;
        return dest;
    }

    /**
     * Multiplies this vector by another vector.
     *
     * @param o vector to multiply by
     * @return this
     */
    public Vec3d mul(Vec3d o) {
        x *= o.x;
        y *= o.y;
        z *= o.z;
        return this;
    }

    /**
     * Multiplies this vector by another vector and stores the result in another vector.
     *
     * @param o vector to multiply by
     * @param dest destination vector
     * @return dest
     */
    public Vec3d mul(Vec3d o, Vec3d dest) {
        dest.x = x * o.x;
        dest.y = y * o.y;
        dest.z = z * o.z;
        return dest;
    }

    /**
     * Divides this vector by a given scalar.
     *
     * @param scalar scalar to divide by
     * @return this
     */
    public Vec3d div(double scalar) {
        x /= scalar;
        y /= scalar;
        z /= scalar;
        return this;
    }

    /**
     * Divides this vector by a given scalar and stores the result in another vector.
     *
     * @param scalar scalar to divide by
     * @param dest destination vector
     * @return dest
     */
    public Vec3d div(double scalar, Vec3d dest) {
        dest.x = x / scalar;
        dest.y = y / scalar;
        dest.z = z / scalar;
        return dest;
    }

    /**
     * Divides this vector by a vector with the given components.
     *
     * @param x x component to divide by
     * @param y y component to divide by
     * @return this
     */
    public Vec3d div(double x, double y, double z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    /**
     * Divides this vector by a vector with the given components, and stores the result in another
     * vector.
     *
     * @param x x component to divide by
     * @param y y component to divide by
     * @param dest destination vector
     * @return dest
     */
    public Vec3d div(double x, double y, double z, Vec3d dest) {
        dest.x = this.x / x;
        dest.y = this.y / y;
        dest.z = this.z / z;
        return dest;
    }

    /**
     * Divides this vector by another vector.
     *
     * @param o vector to divide by
     * @return this
     */
    public Vec3d div(Vec3d o) {
        x /= o.x;
        y /= o.y;
        z /= o.z;
        return this;
    }

    /**
     * Divides this vector by another vector, and stores the result in another vector.
     *
     * @param o vector to divide by
     * @param dest destination vector
     * @return dest
     */
    public Vec3d div(Vec3d o, Vec3d dest) {
        dest.x = x / o.x;
        dest.y = y / o.y;
        dest.z = z / o.z;
        return dest;
    }

    public Vec3d rotateX(Angle angle) {
        CCWAngle ccw = angle.ccw();
        double sin = ccw.sin();
        double cos = ccw.cos();

        double ny = y * cos - z * sin;
        double nz = y * sin + z * cos;
        y = ny;
        z = nz;

        return this;
    }

    public Vec3d rotateY(Angle angle) {
        CCWAngle ccw = angle.ccw();
        double sin = ccw.sin();
        double cos = ccw.cos();

        double nx = x * cos + z * sin;
        double nz = -x * sin + z * cos;
        x = nx;
        z = nz;

        return this;
    }

    public Vec3d rotateZ(Angle angle) {
        CCWAngle ccw = angle.ccw();
        double sin = ccw.sin();
        double cos = ccw.cos();

        double nx = x * cos - y * sin;
        double ny = x * sin + y * cos;
        x = nx;
        y = ny;

        return this;
    }

    /**
     * Computes the magnitude of this vector, squared. This is more efficient than computing the
     * actual magnitude, so prefer this function if you only need to compare the magnitudes of
     * vectors.
     *
     * @return magnitude squared
     */
    public double magnitudeSq() {
        return x * x + y * y + z * z;
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
     * Computes the dot product between this vector and another vector. This is equivalent to the
     * cosine of the angle between them multiplied by their magnitudes.
     *
     * @param o right hand side
     * @return dot product
     */
    public double dot(Vec3d o) {
        return x * o.x + y * o.y + z * o.z;
    }

    /**
     * Computes the distance from this vector to a vector with the specified components, squared.
     * See the note in {@link #magnitudeSq()} about efficiency.
     *
     * @param x x component of other vector
     * @param y y component of other vector
     * @return distance squared
     */
    public double distanceToSq(double x, double y, double z) {
        double dx = this.x - x;
        double dy = this.y - y;
        double dz = this.z - z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Computes the distance from this vector to another vector, squared. See the note in {@link
     * #magnitudeSq()} about efficiency.
     *
     * @param o other vector
     * @return distance squared
     */
    public double distanceToSq(Vec3d o) {
        double dx = x - o.x;
        double dy = y - o.y;
        double dz = z - o.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Computes the distance from this vector to a vector with the specified components.
     *
     * @param x x component of other vector
     * @param y y component of other vector
     * @return distance
     */
    public double distanceTo(double x, double y, double z) {
        return Math.sqrt(distanceToSq(x, y, z));
    }

    /**
     * Computes the distance from this vector to another vector.
     *
     * @param o other vector
     * @return distance
     */
    public double distanceTo(Vec3d o) {
        return Math.sqrt(distanceToSq(o));
    }

    public double[] components() {
        return new double[] {x, y, z};
    }

//    public Vec3d rotateX(Angle a) {
//
//    }

    // TODO
//    public double distanceToLineSegmentSq(Vec3d a, Vec3d b) {
//        double l2 = a.distanceToSq(b);
//        if (l2 == 0) return distanceToSq(a);
//        double t = ((x - a.x) * (b.x - a.x) + (y - a.y) * (b.y - a.y)) / l2;
//        t = MathUtil.clamp(t, 0, 1);
//        return distanceToSq(new Vec3d(MathUtil.lerp(a.x, b.x, t), MathUtil.lerp(a.y, b.y, t)));
//    }
//
//    public double distanceToLineSegment(Vec3d a, Vec3d b) {
//        return Math.sqrt(distanceToLineSegmentSq(a, b));
//    }
//
//    public Vec3d rotateAround(Vec3d axis, Angle angle) {
//
//    }
//
//    public Vec3d rotateAround(Vec3d axis, Angle angle, Vec3d dest) {
//
//    }

    /**
     * Normalizes this vector by making its magnitude 1, thus becoming a unit vector.
     *
     * @return this
     */
    public Vec3d normalize() {
        double mag = magnitude();
        x /= mag;
        y /= mag;
        z /= mag;
        return this;
    }

    /**
     * Normalizes this vector by making its magnitude 1, and stores the result in another vector.
     *
     * @param dest destination vector
     * @return dest
     */
    public Vec3d normalize(Vec3d dest) {
        double mag = magnitude();
        dest.x = x / mag;
        dest.y = y / mag;
        dest.z = z / mag;
        return dest;
    }

    /**
     * Normalizes this vector by scaling it to be along the unit square. This guarantees that at
     * least one of the components will be 1, unless this vector is (0, 0, 0).
     *
     * @return this, normalized to the unit square
     */
    public Vec3d boxNormalize() {
        return boxNormalize(this);
    }

    /**
     * Normalizes this vector by scaling it to be along the unit square and stores the result in
     * another vector. This guarantees that at least one of the components will be 1, unless this
     * vector is (0, 0, 0).
     *
     * @param dest destination vector
     * @return dest
     */
    public Vec3d boxNormalize(Vec3d dest) {
        double max = Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z));
        dest.x = x / max;
        dest.y = y / max;
        dest.z = z / max;
        return dest;
    }

    /**
     * Negates this vector.
     *
     * @return this
     */
    public Vec3d negate() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    /**
     * Negates this vector, and stores the result in another vector.
     *
     * @param dest destination vector
     * @return dest
     */
    public Vec3d negate(Vec3d dest) {
        dest.x = -x;
        dest.y = -y;
        dest.z = -z;
        return dest;
    }

    /**
     * Takes the absolute value of each component of this vector.
     *
     * @return this
     */
    public Vec3d absolute() {
        x = Math.abs(x);
        y = Math.abs(y);
        z = Math.abs(z);
        return this;
    }

    /**
     * Takes the absolute value of each component of this vector, and stores the result in another
     * vector.
     *
     * @param dest destination vector
     * @return dest
     */
    public Vec3d absolute(Vec3d dest) {
        dest.x = Math.abs(x);
        dest.y = Math.abs(y);
        dest.z = Math.abs(z);
        return dest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec3d vec3d = (Vec3d) o;
        return Double.compare(vec3d.x, x) == 0 && Double.compare(vec3d.y, y) == 0 && Double.compare(vec3d.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f, %.3f)", x, y, z);
    }
}
