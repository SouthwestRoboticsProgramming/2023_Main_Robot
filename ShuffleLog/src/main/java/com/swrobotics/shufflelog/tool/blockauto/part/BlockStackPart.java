package com.swrobotics.shufflelog.tool.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.tool.blockauto.BlockAutoTool;
import com.swrobotics.shufflelog.tool.blockauto.BlockStackInst;
import imgui.ImGui;

// TODO
public final class BlockStackPart extends ParamPart {
    @Override
    public Object getDefault() {
        return new BlockStackInst();
    }

    @Override
    public boolean edit(Object[] prev) {
        ImGui.indent();
        boolean changed = ((BlockStackInst) prev[0]).show();
        ImGui.unindent();

        return changed;
    }

    @Override
    public Object duplicateParam(Object param) {
        return ((BlockStackInst) param).duplicate();
    }

    @Override
    public void writeInst(MessageBuilder builder, Object value) {
        ((BlockStackInst) value).write(builder);
    }

    @Override
    public Object readInst(MessageReader reader, BlockAutoTool tool) {
        return BlockStackInst.read(reader, tool);
    }
}
