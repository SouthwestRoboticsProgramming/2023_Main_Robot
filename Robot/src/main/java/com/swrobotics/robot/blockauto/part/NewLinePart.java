package com.swrobotics.robot.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;

public final class NewLinePart implements BlockPart {
    public static final NewLinePart INSTANCE = new NewLinePart();

    private NewLinePart() {}

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(PartTypes.NEW_LINE.getId());
    }
}
