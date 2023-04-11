package com.swrobotics.pathfinding.core.grid;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.pathfinding.task.PathfinderTask;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public final class GridUnion extends Grid {
    private final Set<Grid> children;

    public GridUnion(int width, int height) {
        super(width, height);
        children = new HashSet<>();
    }

    // Cell is passable if all children's cells are passable
    @Override
    public boolean canCellPass(int x, int y) {
        for (Grid grid : children) {
            if (!grid.canCellPass(x, y)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(GridType.UNION.getTypeId());
        builder.addInt(children.size());
        for (Grid child : children) {
            child.addToMessenger(builder);
        }
    }

    public void addGrid(Grid grid) {
        if (grid.getCellWidth() != getCellWidth() || grid.getCellHeight() != getCellHeight())
            throw new IllegalArgumentException("Grid size is not compatible");

        children.add(grid);
        grid.setParent(this);

        invalidateLineOfSightCache();
    }

    public void removeGrid(Grid grid) {
        children.remove(grid);
        grid.setParent(null);
        invalidateLineOfSightCache();
    }

    public Set<Grid> getChildren() {
        return children;
    }

    @Override
    public void register(PathfinderTask task) {
        super.register(task);
        for (Grid child : children) {
            child.register(task);
        }
    }

    public static final class Serializer
            implements JsonSerializer<GridUnion>, JsonDeserializer<GridUnion> {
        @Override
        public GridUnion deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            JsonArray children = obj.getAsJsonArray("children");

            Grid.DeserializationContext ctx = Grid.DESERIALIZATION_CTX.get();
            GridUnion union = new GridUnion(ctx.getWidth(), ctx.getHeight());
            for (JsonElement elem : children) {
                union.addGrid(context.deserialize(elem, Grid.class));
            }

            return union;
        }

        @Override
        public JsonElement serialize(
                GridUnion src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", GridType.UNION.toString());
            JsonArray children = new JsonArray();
            for (Grid child : src.children) {
                children.add(context.serialize(child));
            }
            obj.add("children", children);
            return obj;
        }
    }
}
