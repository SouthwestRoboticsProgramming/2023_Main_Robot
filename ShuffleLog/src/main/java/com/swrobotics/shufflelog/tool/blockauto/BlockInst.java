package com.swrobotics.shufflelog.tool.blockauto;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.shufflelog.tool.blockauto.part.BlockPart;
import com.swrobotics.shufflelog.tool.blockauto.part.BlockStackPart;
import com.swrobotics.shufflelog.tool.blockauto.part.NewLinePart;
import com.swrobotics.shufflelog.tool.blockauto.part.ParamPart;
import com.swrobotics.shufflelog.tool.blockauto.part.StaticPart;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiDragDropFlags;

import java.util.List;

public final class BlockInst {
    private final BlockDef def;
    private final Object[] params;
    private final boolean firstElemRequiresAlignToFrame;
    private BlockStackInst stack;

    // Workaround for ImGui bug with drag and drop
    private boolean valid = true;
    private boolean isPaletteEntry;

    public BlockInst(BlockDef def, Object[] params) {
        this.def = def;
        this.params = params;

        List<BlockPart> parts = def.getParts();
        if (parts.isEmpty() || parts.get(0).isFrame())
            firstElemRequiresAlignToFrame = false;
        else {
            boolean hasFrame = false;
            for (BlockPart part : parts) {
                if (part.isFrame()) {
                    hasFrame = true;
                    break;
                }
            }
            firstElemRequiresAlignToFrame = hasFrame;
        }
    }

    public boolean isValid() {
        return valid || isPaletteEntry;
    }

    public void invalidate() {
        valid = false;
    }

    public void setPalette() {
        isPaletteEntry = true;
    }

    public void write(MessageBuilder builder) {
        builder.addString(def.getName());
        int paramIdx = 0;
        for (BlockPart part : def.getParts()) {
            if (part instanceof ParamPart) {
                ParamPart p = (ParamPart) part;
                p.writeInst(builder, params[paramIdx++]);
            }
        }
    }

    public BlockStackInst getStack() {
        return stack;
    }

    public void setStack(BlockStackInst stack) {
        this.stack = stack;
    }

    public BlockDef getDef() {
        return def;
    }

    private void dragSource() {
        if (ImGui.beginDragDropSource(ImGuiDragDropFlags.SourceAllowNullID)) {
            // Technically is doing more than we need, but it should still work
            draw(() -> {});

            ImGui.setDragDropPayload(BlockAutoTool.BLOCK_DND_ID, this);
            ImGui.endDragDropSource();
        }
    }

    public boolean draw(Runnable ctxMenu) {
        ImDrawList draw = ImGui.getWindowDrawList();

        boolean changed = false;
        draw.channelsSplit(2);
        draw.channelsSetCurrent(1);
        ImGui.beginGroup();
        ImGui.beginGroup();
        int paramIdx = 0;
        boolean first = true;
        List<BlockPart> parts = def.getParts();
        boolean isStack, shouldSameLine = true;
        for (int id = 0; id < parts.size(); id++) {
            if (first) {
                if (firstElemRequiresAlignToFrame)
                    ImGui.alignTextToFramePadding();
                first = false;
            } else if (shouldSameLine) {
                ImGui.sameLine();
            }

            BlockPart part = parts.get(id);

            isStack = part instanceof BlockStackPart;
            shouldSameLine = !isStack && !(part instanceof NewLinePart);
            if (isStack) {
                ImGui.endGroup();
                dragSource();
                ctxMenu.run();
            }

            ImGui.pushID(id);
            if (part instanceof ParamPart) {
                ParamPart p = (ParamPart) part;
                Object[] param = {params[paramIdx]};
                changed |= p.edit(param);
                params[paramIdx] = param[0];
                paramIdx++;
            } else if (part instanceof StaticPart) {
                StaticPart s = (StaticPart) part;
                s.draw();
            }
            ImGui.popID();

            if (isStack) {
                ImGui.beginGroup();
            }
        }
        ImGui.endGroup();
        dragSource();
        ctxMenu.run();
        ImGui.endGroup();
        int border = def.getCategory().getBorderColor();
        int color = def.getCategory().getBgColor();
        float pad = ImGui.getStyle().getItemSpacingY() / 2;
        draw.channelsSetCurrent(0);
        draw.addRectFilled(ImGui.getItemRectMinX()-pad, ImGui.getItemRectMinY()-pad, ImGui.getItemRectMaxX()+pad, ImGui.getItemRectMaxY()+pad, color);
        draw.addRect(ImGui.getItemRectMinX()-pad, ImGui.getItemRectMinY()-pad, ImGui.getItemRectMaxX()+pad, ImGui.getItemRectMaxY()+pad, border);
        draw.channelsMerge();

        return changed;
    }

    public BlockInst duplicate() {
        Object[] dupParams = new Object[params.length];
        int i = 0;
        for (BlockPart part : def.getParts()) {
            if (part instanceof ParamPart) {
                dupParams[i] = ((ParamPart) part).duplicateParam(params[i]);
                i++;
            }
        }
        return new BlockInst(def, dupParams);
    }
}
