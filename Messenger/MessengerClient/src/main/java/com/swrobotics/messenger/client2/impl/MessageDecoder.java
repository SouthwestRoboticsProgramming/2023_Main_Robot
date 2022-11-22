package com.swrobotics.messenger.client2.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class MessageDecoder extends ReplayingDecoder<MessageDecodeState> {
    private byte[] typeBuf;
    private byte[] dataBuf;

    private void reset() {
        checkpoint(MessageDecodeState.TYPE_LENGTH);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        reset();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Intentional fall-through on the switch statement here (no break)
        switch (state()) {
            case TYPE_LENGTH:
                int typeLen = in.readUnsignedShort();
                typeBuf = new byte[typeLen];
                checkpoint(MessageDecodeState.TYPE);
            case TYPE:
                in.readBytes(typeBuf, 0, typeBuf.length);
                checkpoint(MessageDecodeState.DATA_LENGTH);
            case DATA_LENGTH:
                int dataLen = in.readInt();
                dataBuf = new byte[dataLen];
                checkpoint(MessageDecodeState.DATA);
            case DATA:
                in.readBytes(dataBuf, 0, dataBuf.length);
                out.add(new Message(new String(typeBuf, StandardCharsets.UTF_8), dataBuf));
                reset();
                break;
        }
    }
}
