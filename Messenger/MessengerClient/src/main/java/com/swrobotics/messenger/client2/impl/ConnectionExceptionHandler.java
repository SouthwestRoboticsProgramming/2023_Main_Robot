package com.swrobotics.messenger.client2.impl;

import com.swrobotics.messenger.client2.MessengerClient;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public final class ConnectionExceptionHandler extends ChannelDuplexHandler {
    private final MessengerClient client;

    public ConnectionExceptionHandler(MessengerClient client) {
        this.client = client;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("Messenger connection lost:");
        cause.printStackTrace();
        client.reconnect();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        client.reconnect();
        System.err.println("Channel closed");
        super.channelInactive(ctx);
    }
}
