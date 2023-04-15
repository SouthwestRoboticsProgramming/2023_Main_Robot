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

public final class Rectangle extends Shape {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final double rotation;

    public Rectangle(
            double x, double y, double width, double height, double rotation, boolean inverted) {
        super(inverted);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getRotation() {
        return rotation;
    }

    @Override
    public boolean collidesWith(RobotShape robot, double robotX, double robotY) {
        return CollisionChecks.checkRectangleVsCircleRobot(this, (Circle) robot, robotX, robotY);
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        super.writeToMessenger(builder);
        builder.addByte(ShapeType.RECTANGLE.getTypeId());
        builder.addDouble(x);
        builder.addDouble(y);
        builder.addDouble(width);
        builder.addDouble(height);
        builder.addDouble(rotation);
    }

    public static final class Serializer
            implements JsonSerializer<Rectangle>, JsonDeserializer<Rectangle> {
        @Override
        public Rectangle deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            double x = obj.get("x").getAsDouble();
            double y = obj.get("y").getAsDouble();
            double w = obj.get("width").getAsDouble();
            double h = obj.get("height").getAsDouble();
            double r = obj.get("rotation").getAsDouble();
            boolean inverted = obj.has("inverted") && obj.get("inverted").getAsBoolean();
            return new Rectangle(x, y, w, h, r, inverted);
        }

        @Override
        public JsonElement serialize(
                Rectangle src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", ShapeType.RECTANGLE.toString());
            obj.addProperty("x", src.x);
            obj.addProperty("y", src.y);
            obj.addProperty("width", src.width);
            obj.addProperty("height", src.height);
            obj.addProperty("rotation", src.rotation);
            obj.addProperty("inverted", src.isInverted());
            return obj;
        }
    }
}
