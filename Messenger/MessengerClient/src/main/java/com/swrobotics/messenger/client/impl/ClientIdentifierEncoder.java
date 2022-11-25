package com.swrobotics.messenger.client.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

public final class ClientIdentifierEncoder extends MessageToByteEncoder<ClientIdentifier> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ClientIdentifier msg, ByteBuf out) throws Exception {
        byte[] data = msg.getName().getBytes(StandardCharsets.UTF_8);
        out.writeShort(data.length);
        out.writeBytes(data);
    }
}
