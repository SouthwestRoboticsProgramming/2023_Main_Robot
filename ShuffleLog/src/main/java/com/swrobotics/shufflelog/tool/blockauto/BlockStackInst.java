package com.swrobotics.shufflelog.tool.blockauto;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;

public final class BlockStackInst {
    public static BlockStackInst read(MessageReader reader, BlockAutoTool tool) {
        int len = reader.readInt();
        BlockStackInst inst = new BlockStackInst();

        for (int i = 0; i < len; i++) {
            String name = reader.readString();
            BlockDef def = tool.getBlockDef(name);
            BlockInst block = def.readInstance(reader, tool);
            inst.addBlock(block);
        }

        return inst;
    }

    private final List<BlockInst> blocks;

    public BlockStackInst() {
        blocks = new ArrayList<>();
    }

    public void addBlock(BlockInst block) {
        blocks.add(block);
        block.setStack(this);
    }

    public void removeBlock(BlockInst block) {
        blocks.remove(block);
        block.setStack(null);
    }

    private boolean acceptBlock(int insertionIdx) {
        boolean changed = false;
        if (ImGui.beginDragDropTarget()) {
            BlockInst block = ImGui.acceptDragDropPayload(BlockAutoTool.BLOCK_DND_ID);
            if (block != null && block.isValid()) {
                block.invalidate();

                BlockStackInst parent = block.getStack();
                if (parent == this) {
                    if (parent.blocks.indexOf(block) < insertionIdx) {
                        insertionIdx--;
                    }
                    parent.removeBlock(block);
                } else if (parent != null) {
                    parent.removeBlock(block);
                }

                BlockInst dup = block.duplicate();
                blocks.add(insertionIdx, dup);
                dup.setStack(this);

                changed = true;

            }
            ImGui.endDragDropTarget();
        }
        return changed;
    }

    // a secret message in there
    public void write(MessageBuilder builder) {
        builder.addInt(blocks.size());
        for (BlockInst block : blocks) {
            block.write(builder);
        }
    }

    public boolean show() {
        List<BlockInst> blockCopy = new ArrayList<>(blocks);
        int i = 0;
        boolean[] changed = {false};
        for (BlockInst block : blockCopy) {
            if (!blocks.contains(block))
                continue;
            ImGui.pushID(i);
            int[] popupId = {0};
            changed[0] |= block.draw(() -> {
                if (ImGui.beginPopupContextItem("block_popup_" + popupId[0]++)) {
                    if (ImGui.selectable("Delete block (" + block.getDef().getName() + ")")) {
                        removeBlock(block);
                        changed[0] = true;
                    }
                    ImGui.endPopup();
                }
            });
            changed[0] |= acceptBlock(i);
            ImGui.popID();
            i++;
        }

        ImGui.text("Insert block here...");
        changed[0] |= acceptBlock(blockCopy.size());

        return changed[0];
    }

    public BlockStackInst duplicate() {
        BlockStackInst i = new BlockStackInst();
        for (BlockInst block : blocks) {
            i.addBlock(block.duplicate());
        }
        return i;
    }
}
