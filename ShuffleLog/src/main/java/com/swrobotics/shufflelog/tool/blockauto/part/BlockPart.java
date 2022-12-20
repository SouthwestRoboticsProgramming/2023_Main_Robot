package com.swrobotics.shufflelog.tool.blockauto.part;

import com.swrobotics.messenger.client.MessageReader;

public abstract class BlockPart {
    public static BlockPart read(MessageReader reader) {
        byte id = reader.readByte();
        PartTypes type = PartTypes.get(id);
        return type.read(reader);
    }

    public boolean isFrame() {
        return false;
    }
}
