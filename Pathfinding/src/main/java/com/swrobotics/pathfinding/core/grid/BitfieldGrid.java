package com.swrobotics.pathfinding.core.grid;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.swrobotics.messenger.client.MessageBuilder;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.BitSet;

public class BitfieldGrid extends Grid {
    private final BitSet data;

    public BitfieldGrid(int width, int height) {
        super(width, height);
        data = new BitSet(width * height);
        clear();
    }

    private BitfieldGrid(int width, int height, byte[] data) {
        super(width, height);
        this.data = BitSet.valueOf(data);
    }

    public void set(int x, int y, boolean value) {
        data.set(x + y * width, value);
        invalidateLineOfSightCache();
    }

    public void copyFrom(BitfieldGrid other) {
        for (int x = 0; x < width && x < other.width; x++)
            for (int y = 0; y < height && y < other.height; y++) set(x, y, other.canCellPass(x, y));
    }

    @Override
    public boolean canCellPass(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return false;
        return data.get(x + y * width);
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(GridType.BITFIELD.getTypeId());
        writeToMessengerNoTypeId(builder);
    }

    public void writeToMessengerNoTypeId(MessageBuilder builder) {
        builder.addInt(width);
        builder.addInt(height);
        long[] data = this.data.toLongArray();
        builder.addInt(data.length);
        for (long val : data) {
            builder.addLong(val);
        }
    }

    public void clear() {
        data.set(0, data.size());
    }

    public static final class Serializer
            implements JsonSerializer<BitfieldGrid>, JsonDeserializer<BitfieldGrid> {
        @Override
        public BitfieldGrid deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            Grid.DeserializationContext ctx = Grid.DESERIALIZATION_CTX.get();
            byte[] data =
                    Base64.getUrlDecoder().decode(json.getAsJsonObject().get("data").getAsString());
            return new BitfieldGrid(ctx.getWidth(), ctx.getHeight(), data);
        }

        @Override
        public JsonElement serialize(
                BitfieldGrid src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", GridType.BITFIELD.toString());
            obj.addProperty("data", Base64.getUrlEncoder().encodeToString(src.data.toByteArray()));
            return obj;
        }
    }
}
