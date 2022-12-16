package com.swrobotics.pathfinding.geom;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.pathfinding.grid.ShapeGrid;
import com.swrobotics.pathfinding.task.PathfinderTask;

import java.lang.reflect.Type;
import java.util.UUID;

public abstract class Shape {
    private UUID id;
    private ShapeGrid parent;

    public Shape() {
        id = UUID.randomUUID();
    }

    public abstract boolean collidesWith(RobotShape robot, double robotX, double robotY);

    public void writeToMessenger(MessageBuilder builder) {
        builder.addLong(id.getMostSignificantBits());
        builder.addLong(id.getLeastSignificantBits());
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

    public static final class Serializer implements JsonDeserializer<Shape> {
        @Override
        public Shape deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            ShapeType type = ShapeType.valueOf(obj.get("type").getAsString());
            return context.deserialize(obj, type.getType());
        }
    }
}
