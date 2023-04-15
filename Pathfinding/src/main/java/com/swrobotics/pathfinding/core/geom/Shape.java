package com.swrobotics.pathfinding.core.geom;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.pathfinding.core.grid.ShapeGrid;
import com.swrobotics.pathfinding.task.PathfinderTask;

import java.lang.reflect.Type;
import java.util.UUID;

public abstract class Shape {
    private UUID id;
    private ShapeGrid parent;
    private final boolean inverted;

    public Shape(boolean inverted) {
        id = UUID.randomUUID();
        this.inverted = inverted;
    }

    public abstract boolean collidesWith(RobotShape robot, double robotX, double robotY);

    public void writeToMessenger(MessageBuilder builder) {
        builder.addLong(id.getMostSignificantBits());
        builder.addLong(id.getLeastSignificantBits());
        builder.addBoolean(inverted);
    }

    public void register(PathfinderTask task) {
        task.registerShape(this);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ShapeGrid getParent() {
        return parent;
    }

    public void setParent(ShapeGrid parent) {
        this.parent = parent;
    }

    public boolean isInverted() {
        return inverted;
    }

    public static final class Serializer implements JsonDeserializer<Shape> {
        @Override
        public Shape deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            ShapeType type = ShapeType.valueOf(obj.get("type").getAsString());
            return context.deserialize(obj, type.getType());
        }
    }
}
