package com.swrobotics.shufflelog.tool.field.path;

import processing.core.PGraphics;

import java.util.List;

public final class PathFollowerTest {
    private double robotX, robotY;
    private List<Point> path;

    // Must be larger than a pathfinding tile
    double tol = 0.175;

    public void setPath(List<Point> path) {
        this.path = path;
    }

    private double sqr(double x) {
        return x * x;
    }

    private double dist2(double vx, double vy, double wx, double wy) {
        return sqr(vx - wx) + sqr(vy - wy);
    }

    private double distanceToLineSegment(
            double px, double py, double vx, double vy, double wx, double wy) {
        double l2 = dist2(vx, vy, wx, wy);
        if (l2 == 0) return dist2(px, py, vx, vy);
        double t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2;
        t = Math.max(0, Math.min(1, t));
        return Math.sqrt(dist2(px, py, vx + t * (wx - vx), vy + t * (wy - vy)));
    }

    // Returns string representing current status
    public String go() {
        if (path == null || path.size() < 2) return "no path/too short";

        // Check if we can skip to a later part of the path (latency correction)
        // Because of latency, the starting point of the path can be significantly
        // behind the actual location
        Point target = null;
        for (int i = path.size() - 1; i > 0; i--) {
            Point point = path.get(i);
            Point prev = path.get(i - 1);

            double dist = distanceToLineSegment(robotX, robotY, point.x, point.y, prev.x, prev.y);

            // If the robot is close enough to the line, use its endpoint as the target
            if (dist < tol) {
                target = point;
                break;
            }
        }

        // The path is invalid because the robot is nowhere near the path, so leave
        // This can happen if the target changes, but the start position hasn't updated yet due to
        // latency
        if (target == null) return "not near path";

        // Find normal vector towards target
        double deltaX = target.x - robotX;
        double deltaY = target.y - robotY;
        double len = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (len < tol) return "within target tolerance";
        deltaX /= len;
        deltaY /= len;

        // Scale by movement speed
        deltaX *= 0.1;
        deltaY *= 0.1;

        // Move
        robotX += deltaX;
        robotY += deltaY;

        return "moving";
    }

    public void draw(PGraphics g) {
        g.fill(255);
        System.out.println("Robot at " + robotX + ", " + robotY);
        g.rect((float) (robotX - 0.5), (float) (robotY - 0.5), 1, 1);
    }

    public double getX() {
        return robotX;
    }

    public double getY() {
        return robotY;
    }
}
