package com.swrobotics.shufflelog.tool.blockauto;

import com.swrobotics.messenger.client.MessageReader;

import java.util.ArrayList;
import java.util.List;

import static imgui.ImGui.collapsingHeader;
import static imgui.ImGui.popID;
import static imgui.ImGui.pushID;

public final class BlockCategory {
    public static BlockCategory read(MessageReader reader) {
        String name = reader.readString();
        int count = reader.readInt();
        List<BlockDef> blocks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            blocks.add(BlockDef.read(reader));
        }

        return new BlockCategory(name, blocks);
    }

    private final String name;
    private final List<BlockInst> blocks;

    private BlockCategory(String name, List<BlockDef> blocks) {
        this.name = name;
        this.blocks = new ArrayList<>();
        for (BlockDef def : blocks) {
            this.blocks.add(def.instantiate());
        }
        for (BlockInst inst : this.blocks) {
            inst.setPalette();
        }
    }

    public List<BlockInst> getBlocks() {
        return blocks;
    }

    public void draw() {
        if (collapsingHeader(name)) {
            int i = 0;
            for (BlockInst inst : blocks) {
                pushID(i++);
                inst.draw(() -> {});
                popID();
            }
        }
    }
}
