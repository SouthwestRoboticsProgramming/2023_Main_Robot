package com.swrobotics.shufflelog.tool.blockauto.part;

import com.swrobotics.messenger.client.MessageReader;

import java.util.HashMap;
import java.util.Map;

public enum PartTypes {
    TEXT(0) {
        @Override
        public BlockPart read(MessageReader reader) {
            return TextPart.read(reader);
        }
    },
    INT(1) {
        @Override
        public BlockPart read(MessageReader reader) {
            return IntPart.read(reader);
        }
    },
    DOUBLE(2) {
        @Override
        public BlockPart read(MessageReader reader) {
            return DoublePart.read(reader);
        }
    },
    VEC2D(3) {
        @Override
        public BlockPart read(MessageReader reader) {
            return Vec2dPart.read(reader);
        }
    },
    FIELD_POINT(4) {
        @Override
        public BlockPart read(MessageReader reader) {
            return FieldPointPart.read(reader);
        }
    },
    ANGLE(5) {
        @Override
        public BlockPart read(MessageReader reader) {
            return AnglePart.read(reader);
        }
    },
    ENUM(6) {
        @Override
        public BlockPart read(MessageReader reader) {
            return EnumPart.read(reader);
        }
    },
    BLOCK_STACK(7) {
        @Override
        public BlockPart read(MessageReader reader) {
            return new BlockStackPart();
        }
    },
    NEW_LINE(8) {
        @Override
        public BlockPart read(MessageReader reader) {
            return NewLinePart.INSTANCE;
        }
    };

    private static final Map<Byte, PartTypes> BY_ID = new HashMap<>();
    static {
        for (PartTypes p : values()) {
            BY_ID.put(p.id, p);
        }
    }

    public static PartTypes get(byte id) {
        return BY_ID.get(id);
    }

    private final byte id;

    PartTypes(int id) {
        this.id = (byte) id;
    }

    public abstract BlockPart read(MessageReader reader);
}
