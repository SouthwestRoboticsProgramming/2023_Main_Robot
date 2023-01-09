package com.swrobotics.shufflelog.tool.blockauto;

import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.math.MathUtils;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;

import static imgui.ImGui.collapsingHeader;
import static imgui.ImGui.popID;
import static imgui.ImGui.pushID;

public final class BlockCategory {
    public static BlockCategory read(MessageReader reader) {
        String name = reader.readString();
        byte r = reader.readByte();
        byte g = reader.readByte();
        byte b = reader.readByte();
        int count = reader.readInt();
        List<BlockDef> blocks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            blocks.add(BlockDef.read(reader));
        }

        return new BlockCategory(name, blocks, r, g, b);
    }

    private static final float BORDER_BRIGHTEN_AMT = 0.1f;

    private final String name;
    private final List<BlockInst> blocks;
    private final int bgColor, borderColor;

    private BlockCategory(String name, List<BlockDef> blocks, byte r, byte g, byte b) {
        this.name = name;
        this.blocks = new ArrayList<>();
        for (BlockDef def : blocks) {
            def.setCategory(this);
            this.blocks.add(def.instantiate());
        }
        for (BlockInst inst : this.blocks) {
            inst.setPalette();
        }

        float fR = (r & 0xFF) / 255f;
        float fG = (g & 0xFF) / 255f;
        float fB = (b & 0xFF) / 255f;
        bgColor = ImGui.colorConvertFloat4ToU32(fR, fG, fB, 1.0f);
        borderColor = ImGui.colorConvertFloat4ToU32(
                MathUtils.clamp(fR + BORDER_BRIGHTEN_AMT, 0, 1),
                MathUtils.clamp(fG + BORDER_BRIGHTEN_AMT, 0, 1),
                MathUtils.clamp(fB + BORDER_BRIGHTEN_AMT, 0, 1),
                1.0f
        );
    }

    public List<BlockInst> getBlocks() {
        return blocks;
    }

    public int getBgColor() {
        return bgColor;
    }

    public int getBorderColor() {
        return borderColor;
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
