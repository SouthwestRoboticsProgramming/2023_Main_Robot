package com.swrobotics.robot.blockauto;

import com.swrobotics.messenger.client.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

public final class BlockCategory {
    private final String name;
    private final List<BlockDef> blocks;
    private final byte r, g, b;

    public BlockCategory(String name, byte r, byte g, byte b) {
        this.name = name;
        blocks = new ArrayList<>();
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public BlockDef newBlock(String name) {
        BlockDef def = new BlockDef(name);
        blocks.add(def);
        return def;
    }

    public List<BlockDef> getBlocks() {
        return blocks;
    }

    public void writeToMessenger(MessageBuilder builder) {
        builder.addString(name);
        builder.addByte(r);
        builder.addByte(g);
        builder.addByte(b);
        builder.addInt(blocks.size());
        for (BlockDef def : blocks) {
            def.writeToMessenger(builder);
        }
    }
}
