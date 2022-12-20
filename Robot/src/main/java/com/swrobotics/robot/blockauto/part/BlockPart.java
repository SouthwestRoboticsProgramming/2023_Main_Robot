package com.swrobotics.robot.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;

public interface BlockPart {
    /**
     * Must write the corresponding type ID from {@link PartTypes},
     * then optionally any additional data.
     * 
     * @param builder builder to write to
     */
    void writeToMessenger(MessageBuilder builder);
}
