package com.swrobotics.shufflelog.tool.blockauto;

import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.tool.blockauto.part.BlockPart;
import com.swrobotics.shufflelog.tool.blockauto.part.ParamPart;

import java.util.ArrayList;
import java.util.List;

public final class BlockDef {
    public static BlockDef read(MessageReader reader) {
        String name = reader.readString();
        int count = reader.readInt();
        List<BlockPart> parts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            parts.add(BlockPart.read(reader));
        }

        return new BlockDef(name, parts);
    }

    private final String name;
    private final List<BlockPart> parts;

    public BlockDef(String name, List<BlockPart> parts) {
        this.name = name;
        this.parts = parts;
    }

    public String getName() {
        return name;
    }

    public List<BlockPart> getParts() {
        return parts;
    }

    public BlockInst readInstance(MessageReader reader, BlockAutoTool tool) {
        List<Object> params = new ArrayList<>();
        for (BlockPart part : parts) {
            if (part instanceof ParamPart) {
                ParamPart p = (ParamPart) part;
                params.add(p.readInst(reader, tool));
            }
        }
        return new BlockInst(this, params.toArray());
    }

    public BlockInst instantiate() {
        List<Object> params = new ArrayList<>();
        for (BlockPart part : parts) {
            if (part instanceof ParamPart) {
                ParamPart p = (ParamPart) part;
                params.add(p.getDefault());
            }
        }
        return new BlockInst(this, params.toArray());
    }
}
