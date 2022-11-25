package com.swrobotics.messenger.client.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

public final class MessageEncoder extends MessageToByteEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        String type = msg.getType();
        byte[] data = msg.getData();

        byte[] typeUTF = type.getBytes(StandardCharsets.UTF_8);
        out.writeShort(typeUTF.length);
        out.writeBytes(typeUTF);

        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
