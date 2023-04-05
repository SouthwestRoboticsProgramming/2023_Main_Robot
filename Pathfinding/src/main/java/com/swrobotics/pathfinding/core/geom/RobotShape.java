package com.swrobotics.pathfinding.core.geom;

/** Indicates that a shape can be used as a robot collider */
public abstract class RobotShape extends Shape {
    public RobotShape(boolean inverted) {
        super(inverted);
    }
}
