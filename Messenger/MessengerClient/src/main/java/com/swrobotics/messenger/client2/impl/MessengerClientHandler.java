package com.swrobotics.messenger.client2.impl;

import com.swrobotics.messenger.client2.MessengerClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public final class MessengerClientHandler extends ChannelInboundHandlerAdapter {
    public static final String HEARTBEAT = "_Heartbeat";
    public static final String LISTEN = "_Listen";
    public static final String DISCONNECT = "_Disconnect";

    private static final long HEARTBEAT_INTERVAL = 1;

    private final MessengerClient client;
    private final ClientIdentifier id;
    private final Queue<Message> incomingMessageQueue;
    private final List<String> listening;

    public MessengerClientHandler(MessengerClient client, ClientIdentifier id, Queue<Message> incomingMessageQueue, List<String> listening) {
        this.client = client;
        this.id = id;
        this.incomingMessageQueue = incomingMessageQueue;
        this.listening = listening;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.writeAndFlush(id);
        ctx.executor().scheduleAtFixedRate(() -> {
            ctx.writeAndFlush(new Message(HEARTBEAT, new byte[0]));
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
        for (String listen : listening) {
            client.listen(listen);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Message) {
            System.out.println("Got message");
            incomingMessageQueue.add((Message) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
