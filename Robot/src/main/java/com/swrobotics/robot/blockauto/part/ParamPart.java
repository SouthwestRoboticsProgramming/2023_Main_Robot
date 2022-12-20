package com.swrobotics.robot.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

public interface ParamPart extends BlockPart {
    Object readInst(MessageReader reader);
    void writeInst(MessageBuilder builder, Object val);
}
