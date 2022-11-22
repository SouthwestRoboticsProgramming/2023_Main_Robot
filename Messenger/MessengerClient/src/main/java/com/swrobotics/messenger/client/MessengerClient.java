package com.swrobotics.messenger.client;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a connection to the Messenger server.
 * This can be used to send messages between processes.
 *
 * @author rmheuer
 */
public final class MessengerClient {
    private static final String HEARTBEAT = "_Heartbeat";
    private static final String LISTEN = "_Listen";
    private static final String UNLISTEN = "_Unlisten";
    private static final String DISCONNECT = "_Disconnect";

    private static final long TIMEOUT = 1000L; // Timeout in milliseconds

    private String host;
    private int port;
    private String name;

    private Selector selector;
    private SocketChannel channel;
    private SelectionKey selectKey;

    private final AtomicBoolean connected;
    private final ScheduledExecutorService executor;
    private final ScheduledFuture<?> heartbeatFuture;
    private Thread connectThread;

    private final Set<String> listening;
    private final Set<Handler> handlers;

    private Exception lastConnectFailException;

    /**
     * Creates a new instance and attempts to connect to a
     * Messenger server at the given address.
     *
     * @param host server host
     * @param port server port
     * @param name unique string used in logging 
     */
    public MessengerClient(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;

        selector = null;
        channel = null;
        selectKey = null;
        connected = new AtomicBoolean(false);

        executor = Executors.newSingleThreadScheduledExecutor();
        heartbeatFuture = executor.scheduleAtFixedRate(() -> {
            sendMessage(HEARTBEAT, new byte[0]);
        }, 0, 1, TimeUnit.SECONDS);

        listening = Collections.synchronizedSet(new HashSet<>());
        handlers = new HashSet<>();

        lastConnectFailException = null;

        startConnectThread();
    }

    /**
     * Attempts to reconnect to a different Messenger server at
     * a given address.
     *
     * @param host server host
     * @param port server port
     * @param name unique string used in logging
     */
    public void reconnect(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;

        send(DISCONNECT);
        disconnectSocket();
        connected.set(false);

        startConnectThread();
    }

    /**
     * Gets the last exception thrown when attempting to connect.
     * If no attempts have failed, this will return {@code null}.
     *
     * @return last connection exception
     */
    public Exception getLastConnectionException() {
        return lastConnectFailException;
    }

    private void startConnectThread() {
        connectThread = new Thread(() -> {
            while (!connected.get() && !Thread.interrupted()) {
                try {
                    System.out.println("Opening");
                    selector = Selector.open();
                    channel = SocketChannel.open();
                    channel.configureBlocking(false);
                    System.out.println("Connecting");
                    if (!channel.connect(new InetSocketAddress(host, port))) {
                        // We reach here if not immediately connected
                        selectKey = channel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE | SelectionKey.OP_READ);

                        selector.select(TIMEOUT);
                        if (selectKey.isConnectable()) {
                            channel.finishConnect();
//                            selectKey.cancel();
                        } else {
                            throw new IOException("Connection timed out: " + host + ":" + port);
                        }
                    }

                    System.out.println("Setting up");
//                    selectKey = channel.register(selector, SelectionKey.OP_WRITE);

//                    socket = new Socket();
//                    socket.setSoTimeout(1000);
//                    socket.connect(new InetSocketAddress(host, port), TIMEOUT);
//                    in = new DataInputStream(socket.getInputStream());
//                    out = new DataOutputStream(socket.getOutputStream());

                    // Send name
                    System.out.println("Sending name");
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    new DataOutputStream(b).writeUTF(name);
                    byte[] data = b.toByteArray();
                    ByteBuffer buf = ByteBuffer.allocate(data.length);
                    buf.put(data);
                    buf.flip();
                    sendWithTimeout(buf);

//                    out.writeUTF(name);

                    System.out.println("Good");
                    connected.set(true);

                    for (String listen : listening) {
                        listen(listen);
                    }
                } catch (Exception e) {
                    lastConnectFailException = e;
                    System.err.println("Messenger connection failed (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

            connectThread = null;
        }, "Messenger Reconnect Thread");

        connectThread.start();
    }

    private void handleError(IOException e) {
        disconnectSocket();

        System.err.println("Messenger connection lost:");
        e.printStackTrace();

        connected.set(false);
        startConnectThread();
    }

    private void disconnectSocket() {
        if (connectThread != null)
            connectThread.interrupt();
        connectThread = null;

        try {
            channel.close();
            selectKey.cancel();
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ByteBuffer blockingRead(int count) throws IOException {
        long startTime = System.currentTimeMillis();
        ByteBuffer buf = ByteBuffer.allocate(count);
        while (buf.remaining() > 0) {
            if (System.currentTimeMillis() - startTime > TIMEOUT)
                throw new IOException("Read timed out");

            channel.read(buf);
        }
        buf.flip();
        return buf;
    }

    /**
     * Reads all incoming messages. If not connected, this will do
     * nothing. Message handlers will be invoked from this method.
     */
    public void readMessages() {
        if (!isConnected())
            return;

        try {
            while (true) {
                selector.selectNow();
                if (!selectKey.isReadable())
                    break;

                ByteBuffer typeLenBuf = blockingRead(2);
                int typeLen = typeLenBuf.get(0) << 8 | typeLenBuf.get(1);
                System.out.println("Type len: " + typeLen);
                ByteBuffer typeBuf = blockingRead(typeLen);
                byte[] typeData = new byte[typeLen];
                typeBuf.get(typeData);
                String type = new String(typeData, StandardCharsets.UTF_8);

                ByteBuffer dataLenBuf = blockingRead(4);
                int dataLen = dataLenBuf.get(0) << 24 |
                        dataLenBuf.get(1) << 16 |
                        dataLenBuf.get(2) << 8 |
                        dataLenBuf.get(3);
                ByteBuffer dataBuf = blockingRead(dataLen);
                byte[] data = new byte[dataLen];
                dataBuf.get(data);

                for (Handler handler : handlers) {
                    handler.handle(type, data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            while (in.available() > 0) {
//                String type = in.readUTF();
//                int dataSize = in.readInt();
//                byte[] data = new byte[dataSize];
//                in.readFully(data);
//
//                for (Handler handler : handlers) {
//                    handler.handle(type, data);
//                }
//            }
//        } catch (IOException e) {
//            handleError(e);
//        }
    }

    /**
     * Gets whether this client is currently connected to a server.
     *
     * @return connected
     */
    public boolean isConnected() {
        return connected.get();
    }

    /**
     * Disconnects from the current server. After this method is called,
     * this object should no longer be used. If you want to change servers,
     * use {@link #reconnect}.
     */
    public void disconnect() {
        send(DISCONNECT);

        heartbeatFuture.cancel(false);
        executor.shutdown();

        disconnectSocket();
        connected.set(false);
    }

    /**
     * Prepares to send a message. This returns a {@link MessageBuilder},
     * which allows you to add data to the message.
     *
     * @param type type of the message to send
     * @return builder to add data
     */
    public MessageBuilder prepare(String type) {
        return new MessageBuilder(this, type);
    }

    /**
     * Immediately sends a message with no data.
     *
     * @param type type of the message to send
     */
    public void send(String type) {
        sendMessage(type, new byte[0]);
    }

    /**
     * Registers a {@link MessageHandler} to handle incoming messages.
     * If the type ends in '*', the handler will be invoked for all messages
     * that match the content before. For example, "Foo*" would match a
     * message of type "Foo2", while "Foo" would only match messages of
     * type "Foo".
     *
     * @param type type of message to listen to
     * @param handler handler to invoke
     */
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

            if (connected.get()) {
                listen(type);
            }
        }
    }

    private void listen(String type) {
        prepare(LISTEN)
                .addString(type)
                .send();
    }

    void sendMessage(String type, byte[] data) {
        if (!connected.get())
            return;

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(b);
        try {
            d.writeUTF(type);
            d.writeInt(data.length);
            d.write(data);

            byte[] packet = b.toByteArray();
            ByteBuffer buf = ByteBuffer.allocate(packet.length);
            buf.put(packet);
            buf.flip();

            sendWithTimeout(buf);
        } catch (IOException e) {
            handleError(e);
        }
//        synchronized (out) {
//            try {
//                out.writeUTF(type);
//                out.writeInt(data.length);
//                out.write(data);
//            } catch (IOException e) {
//                handleError(e);
//            }
//        }
    }

    private void sendWithTimeout(ByteBuffer data) throws IOException {
        synchronized (channel) {
            int written = channel.write(data);
            if (written == 0) {
                selector.select(TIMEOUT);
                if (!selectKey.isWritable())
                    throw new IOException("Write timed out");
            }
        }
    }

    private interface Handler {
        void handle(String type, byte[] data);
    }

    // Handles simple patterns (type must match exactly)
    private static final class DirectHandler implements Handler {
        private final String targetType;
        private final MessageHandler handler;

        public DirectHandler(String targetType, MessageHandler handler) {
            this.targetType = targetType;
            this.handler = handler;
        }

        @Override
        public void handle(String type, byte[] data) {
            if (type.equals(targetType)) {
                handler.handle(type, new MessageReader(data));
            }
        }
    }

    // Handles wildcard patterns (i.e. patterns that end in '*')
    private static final class WildcardHandler implements Handler {
        private final String targetPrefix;
        private final MessageHandler handler;

        public WildcardHandler(String targetPrefix, MessageHandler handler) {
            this.targetPrefix = targetPrefix;
            this.handler = handler;
        }

        @Override
        public void handle(String type, byte[] data) {
            if (type.startsWith(targetPrefix)) {
                handler.handle(type, new MessageReader(data));
            }
        }
    }
}
