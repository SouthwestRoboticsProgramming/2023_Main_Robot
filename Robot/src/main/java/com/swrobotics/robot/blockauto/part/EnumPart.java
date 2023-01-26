package com.swrobotics.robot.blockauto.part;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

public final class EnumPart<E extends Enum<E>> extends ParamPart {
    private final Class<E> type;
    private final E[] values;
    private final int defIdx;

    public EnumPart(String name, Class<E> type, E def) {
        super(name);
        this.type = type;
        values = type.getEnumConstants();
        int defIdx = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(def)) {
                defIdx = i;
            }
        }
        this.defIdx = defIdx;
    }

    @Override
    public Object readInst(MessageReader reader) {
        int i = reader.readInt();
        return values[i];
    }

    private int indexOf(Object val) {
        @SuppressWarnings("unchecked")
        E e = (E) val;
        int idx = 0;
        boolean found = false;
        for (E v : values) {
            if (v == e) {
                found = true;
                break;
            }
            idx++;
        }
        if (!found)
            throw new IllegalStateException("Failed to find index of value");
        return idx;
    }

    @Override
    public void writeInst(MessageBuilder builder, Object val) {
        builder.addInt(indexOf(val));
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(PartTypes.ENUM.getId());
        builder.addInt(values.length);
        for (E value : values) {
            builder.addString(value.name());
        }
        builder.addInt(defIdx);
    }

    @Override
    public Object deserializeInst(JsonElement elem, JsonDeserializationContext ctx) {
        if (elem == null)
            return values[defIdx];

        try {
            return Enum.valueOf(type, elem.getAsString());
        } catch (IllegalArgumentException e) {
            return values[defIdx];
        }
    }

    @Override
    public JsonElement serializeInst(Object val, JsonSerializationContext ctx) {
        return new JsonPrimitive(((Enum<?>) val).name());
    }
}
