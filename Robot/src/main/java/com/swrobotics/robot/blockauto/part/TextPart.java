package com.swrobotics.robot.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;

public final class TextPart implements BlockPart {
    private final String text;

    public TextPart(String text) {
        this.text = text;
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(PartTypes.TEXT.getId());
        builder.addString(text);
    }
}
