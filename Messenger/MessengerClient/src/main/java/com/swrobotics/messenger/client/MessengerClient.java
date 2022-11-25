package com.swrobotics.messenger.client;

import com.swrobotics.messenger.client.impl.*;
import com.swrobotics.messenger.client.impl.handler.DirectHandler;
import com.swrobotics.messenger.client.impl.handler.Handler;
import com.swrobotics.messenger.client.impl.handler.WildcardHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// FIXME: Reconnect if connection lost
public final class MessengerClient {
    private static final int TIMEOUT = 2;

    private final EventLoopGroup workerGroup;
    private final Queue<Message> incomingMessageQueue;
    private Channel channel;

    private final List<Handler> handlers;
    private final List<String> listening;

    private String host;
    private int port;
    private String name;

    private long prevHeartbeatTimestamp;
    private boolean shouldReconnect;
    private boolean isReconnecting;

    private ScheduledFuture<?> reconnectFuture;

    public MessengerClient(String host, int port, String name) {
        workerGroup = new NioEventLoopGroup();
        incomingMessageQueue = new ConcurrentLinkedQueue<>();

        handlers = new ArrayList<>();
        listening = new ArrayList<>();

        this.host = host;
        this.port = port;
        this.name = name;

        shouldReconnect = true;
        isReconnecting = false;

        connect();
    }

    private void closeCurrentChannel() {
        if (!isConnected())
            return;

        channel.writeAndFlush(new Message(MessengerClientHandler.DISCONNECT, new byte[0]));
        channel.close();
        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        if (!shouldReconnect)
            return;

        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(
                        new ReadTimeoutHandler(TIMEOUT),
                        new WriteTimeoutHandler(TIMEOUT),
                        new ClientIdentifierEncoder(),
                        new MessageEncoder(),
                        new MessageDecoder(),
                        new MessengerClientHandler(MessengerClient.this, new ClientIdentifier(name), incomingMessageQueue, listening),
                        new ConnectionExceptionHandler(MessengerClient.this)
                );
            }
        });

        ChannelFuture f = b.connect(host, port);
        try {
            f.await(TIMEOUT * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        channel = f.channel();
        if (!f.isSuccess()) {
            System.err.println("Messenger connection failed");
            f.cancel(false);
            reconnectFuture = workerGroup.schedule((Runnable) this::reconnect, 1, TimeUnit.SECONDS);
        }

        prevHeartbeatTimestamp = System.currentTimeMillis();
    }

    public void reconnect() {
        if (isReconnecting) {
            return;
        }

        isReconnecting = true;

        closeCurrentChannel();
        connect();

        isReconnecting = false;
        reconnectFuture = null;
    }

    public void reconnect(String host, int port, String name) {
        closeCurrentChannel();
        if (isReconnecting)
            reconnectFuture.cancel(true);

        this.host = host;
        this.port = port;
        this.name = name;
        connect();
    }

    public void readMessages() {
        if (!isConnected()) {
            return;
        }

        // Fix for issue #4: Reconnect if the server hasn't responded to a heartbeat for a while
        if (System.currentTimeMillis() - prevHeartbeatTimestamp > TIMEOUT * 1000) {
            System.err.println("Messenger server timed out");
            reconnect();
        }

        Message msg;
        while ((msg = incomingMessageQueue.poll()) != null) {
            if (msg.getType().equals(MessengerClientHandler.HEARTBEAT)) {
                prevHeartbeatTimestamp = System.currentTimeMillis();
            }

            for (Handler handler : handlers) {
                handler.handle(msg.getType(), msg.getData());
            }
        }
    }

    public boolean isConnected() {
        return channel.isActive();
    }

    public MessageBuilder prepare(String type) {
        return new MessageBuilder(this, type);
    }

    public void send(String type) {
        sendMessage(type, new byte[0]);
    }

    void sendMessage(String type, byte[] data) {
        if (!isConnected())
            return;
        channel.writeAndFlush(new Message(type, data));
    }

    public void addHandler(String type, MessageHandler handler) {
        Handler h;
        if (type.endsWith("*")) {
            h = new WildcardHandler(type.substring(0, type.length() - 1), handler);
        } else {
            h = new DirectHandler(type, handler);
        }
        handlers.add(h);

        if (!listening.contains(type)) {
            listening.add(type);

            if (isConnected()) {
                listen(type);
            }
        }
    }

    public void listen(String type) {
        prepare(MessengerClientHandler.LISTEN)
                .addString(type)
                .send();
    }

    public void disconnect() {
        shouldReconnect = false;

        if (isConnected()) {
            closeCurrentChannel();
        }

        workerGroup.shutdownGracefully();
    }
}
