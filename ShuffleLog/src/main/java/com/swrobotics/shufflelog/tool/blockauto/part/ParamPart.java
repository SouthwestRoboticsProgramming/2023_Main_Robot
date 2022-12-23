package com.swrobotics.shufflelog.tool.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.tool.blockauto.BlockAutoTool;

public abstract class ParamPart extends BlockPart {
    public abstract Object getDefault();

    // Show ImGui editor tool
    public abstract boolean edit(Object[] val);

    public abstract Object readInst(MessageReader reader, BlockAutoTool tool);
    public abstract void writeInst(MessageBuilder builder, Object value);

    public Object duplicateParam(Object param) {
        return param;
    }
}
