package com.swrobotics.pathfinding.geom;

public final class CollisionChecks {
    public static boolean checkCircleVsCircleRobot(Circle obj, Circle robot, double robotX, double robotY) {
        double dx = obj.getX() - (robot.getX() + robotX);
        double dy = obj.getY() - (robot.getY() + robotY);

        double radiusTotalSqr = obj.getRadius() + robot.getRadius();
        radiusTotalSqr *= radiusTotalSqr;

        double distanceSqr = dx * dx + dy * dy;

        return distanceSqr <= radiusTotalSqr;
    }

    public static boolean checkRectangleVsCircleRobot(Rectangle obj, Circle robot, double robotX, double robotY) {
        double lx = (robot.getX() + robotX) - obj.getX();
        double ly = (robot.getY() + robotY) - obj.getY();

        double sin = Math.sin(obj.getRotation() + Math.PI / 2);
        double cos = Math.cos(obj.getRotation() + Math.PI / 2);
        double relativeX = lx * sin - ly * cos;
        double relativeY = lx * cos + ly * sin;

        double halfWidth = obj.getWidth() / 2.0;
        double halfHeight = obj.getHeight() / 2.0;
        double closestX = Math.max(-halfWidth, Math.min(relativeX, halfWidth));
        double closestY = Math.max(-halfHeight, Math.min(relativeY, halfHeight));

        double dx = relativeX - closestX;
        double dy = relativeY - closestY;
        double rad = robot.getRadius();
        return dx * dx + dy * dy <= rad * rad;
    }
}
