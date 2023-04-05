package com.swrobotics.pathfinding.core.geom;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.swrobotics.messenger.client.MessageBuilder;

import java.lang.reflect.Type;

public final class Circle extends RobotShape {
    private final double x;
    private final double y;
    private final double radius;

    public Circle(double x, double y, double radius, boolean inverted) {
        super(inverted);
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public boolean collidesWith(RobotShape robot, double robotX, double robotY) {
        return CollisionChecks.checkCircleVsCircleRobot(this, (Circle) robot, robotX, robotY);
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        super.writeToMessenger(builder);
        builder.addByte(ShapeType.CIRCLE.getTypeId());
        builder.addDouble(x);
        builder.addDouble(y);
        builder.addDouble(radius);
    }

    public static final class Serializer
            implements JsonSerializer<Circle>, JsonDeserializer<Circle> {
        @Override
        public Circle deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            boolean inverted = obj.has("inverted") && obj.get("inverted").getAsBoolean();
            double x = obj.get("x").getAsDouble();
            double y = obj.get("y").getAsDouble();
            double radius = obj.get("radius").getAsDouble();
            return new Circle(x, y, radius, inverted);
        }

        @Override
        public JsonElement serialize(Circle src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("inverted", src.isInverted());
            obj.addProperty("type", ShapeType.CIRCLE.toString());
            obj.addProperty("x", src.x);
            obj.addProperty("y", src.y);
            obj.addProperty("radius", src.radius);
            return obj;
        }
    }
}
