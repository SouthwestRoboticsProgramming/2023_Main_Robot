package com.swrobotics.messenger.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a connection to the Messenger server. This can be used to send messages between
 * processes.
 *
 * @author rmheuer
 */
public final class MessengerClient {
    public static final String EVENT_TYPE = "Messenger:Event";
    public static final String GET_CLIENTS_TYPE = "Messenger:GetClients";
    public static final String CLIENT_LIST_TYPE = "Messenger:Clients";

    private static final String HEARTBEAT = "_Heartbeat";
    private static final String LISTEN = "_Listen";
    private static final String DISCONNECT = "_Disconnect";

    private static final long TIMEOUT = 4000L;

    static String readStringUtf8(DataInputStream in) throws IOException {
        int len = in.readUnsignedShort();
        byte[] utf8 = new byte[len];
        in.readFully(utf8);
        return new String(utf8, StandardCharsets.UTF_8);
    }

    static void writeStringUtf8(DataOutputStream out, String str) throws IOException {
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        out.writeShort(utf8.length);
        out.write(utf8);
    }

    private String host;
    private int port;
    private String name;

    private final AtomicBoolean connected;
    private final ScheduledExecutorService executor;
    private final ScheduledFuture<?> heartbeatFuture;
    private Thread connectThread;

    private final Thread watchdogThread;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final Set<String> listening;
    private final Set<Handler> handlers;
    private final Set<Runnable> disconnectHandlers;

    private Exception lastConnectFailException;

    private long prevServerHeartbeatTimestamp;

    /**
     * Creates a new instance and attempts to connect to a Messenger server at the given address.
     *
     * @param host server host
     * @param port server port
     * @param name unique string used in logging
     */
    public MessengerClient(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;

        socket = null;
        connected = new AtomicBoolean(false);

        executor = Executors.newSingleThreadScheduledExecutor();
        heartbeatFuture =
                executor.scheduleAtFixedRate(
                        () -> {
                            sendMessage(HEARTBEAT, new byte[0]);
                        },
                        0,
                        1,
                        TimeUnit.SECONDS);

        listening = Collections.synchronizedSet(new HashSet<>());
        handlers = new HashSet<>();
        disconnectHandlers = new HashSet<>();

        lastConnectFailException = null;

        startConnectThread();
        watchdogThread = startWatchdog();
    }

    /**
     * Attempts to reconnect to a different Messenger server at a given address.
     *
     * @param host server host
     * @param port server port
     * @param name unique string used in logging
     */
    public void reconnect(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;

        if (connected.get()) {
            send(DISCONNECT);
            disconnectSocket();
            connected.set(false);
        }

        startConnectThread();
    }

    /**
     * Gets the last exception thrown when attempting to connect. If no attempts have failed, this
     * will return {@code null}.
     *
     * @return last connection exception
     */
    public Exception getLastConnectionException() {
        return lastConnectFailException;
    }

    private void startConnectThread() {
        connectThread =
                new Thread(
                        () -> {
                            while (!connected.get() && !Thread.interrupted()) {
                                try {
                                    socket = new Socket();
                                    socket.setSoTimeout(1000);
                                    socket.connect(new InetSocketAddress(host, port), 1000);
                                    in = new DataInputStream(socket.getInputStream());
                                    out = new DataOutputStream(socket.getOutputStream());
                                    writeStringUtf8(out, name);

                                    connected.set(true);
                                    System.out.println("Messenger connection established");

                                    // Prioritize listening to EVENT_TYPE so that we receive our own
                                    // events
                                    // when listening to the rest of them
                                    Set<String> listeningCopy = new HashSet<>(listening);
                                    if (listeningCopy.contains(EVENT_TYPE)) listen(EVENT_TYPE);

                                    for (String listen : listeningCopy) {
                                        if (listen.equals(EVENT_TYPE)) continue;
                                        listen(listen);
                                    }
                                } catch (Exception e) {
                                    lastConnectFailException = e;
                                    System.err.println(
                                            "Messenger connection failed ("
                                                    + e.getClass().getSimpleName()
                                                    + ": "
                                                    + e.getMessage()
                                                    + ")");
                                    connected.set(false);
                                }

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }

                            connectThread = null;
                        },
                        "Messenger Reconnect Thread");

        connectThread.start();
    }

    private Thread startWatchdog() {
        prevServerHeartbeatTimestamp = -1;
        Thread thr =
                new Thread(
                        () -> {
                            while (!Thread.interrupted()) {
                                if (prevServerHeartbeatTimestamp != -1 && connected.get()) {
                                    if (System.currentTimeMillis() - prevServerHeartbeatTimestamp
                                            > TIMEOUT) {
                                        System.err.println(
                                                "Messenger watchdog: Force-closing socket due to"
                                                        + " server timeout");
                                        if (!socket.isClosed()) {
                                            disconnectSocket();
                                            lastConnectFailException =
                                                    new TimeoutException("Server timed out");
                                        }
                                    }
                                }

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }
                        });
        thr.start();
        return thr;
    }

    private void handleError(IOException e) {
        disconnectSocket();

        System.err.println("Messenger connection lost:");
        e.printStackTrace();

        connected.set(false);
        startConnectThread();
    }

    private void disconnectSocket() {
        if (connectThread != null) connectThread.interrupt();
        connectThread = null;

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Runnable handler : disconnectHandlers) {
            handler.run();
        }

        prevServerHeartbeatTimestamp = -1;
        connected.set(false);
    }

    /**
     * Reads all incoming messages. If not connected, this will do nothing. Message handlers will be
     * invoked from this method.
     */
    public void readMessages() {
        if (!isConnected()) {
            if (connectThread == null) startConnectThread();

            return;
        }

        try {
            while (in.available() > 0) {
                String type = readStringUtf8(in);
                int dataSize = in.readInt();
                byte[] data = new byte[dataSize];
                in.readFully(data);

                if (type.equals(HEARTBEAT)) {
                    prevServerHeartbeatTimestamp = System.currentTimeMillis();
                } else {
                    for (Handler handler : handlers) {
                        try {
                            handler.handle(type, data);
                        } catch (Throwable t) {
                            System.err.println("Error in message handler " + t.toString() + ":");
                            t.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            handleError(e);
        }
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
     * Disconnects from the current server. After this method is called, this object should no
     * longer be used. If you want to change servers, use {@link #reconnect}.
     */
    public void disconnect() {
        send(DISCONNECT);

        heartbeatFuture.cancel(false);
        executor.shutdown();

        disconnectSocket();
        connected.set(false);

        watchdogThread.interrupt();
    }

    /**
     * Prepares to send a message. This returns a {@link MessageBuilder}, which allows you to add
     * data to the message.
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
     * Registers a {@link MessageHandler} to handle incoming messages. If the type ends in '*', the
     * handler will be invoked for all messages that match the content before. For example, "Foo*"
     * would match a message of type "Foo2", while "Foo" would only match messages of type "Foo".
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

    /**
     * Registers a function to call whenever the client disconnects.
     *
     * @param handler handler to call
     */
    public void addDisconnectHandler(Runnable handler) {
        disconnectHandlers.add(handler);
    }

    private void listen(String type) {
        prepare(LISTEN).addString(type).send();
    }

    void sendMessage(String type, byte[] data) {
        if (!connected.get()) return;

        synchronized (out) {
            try {
                writeStringUtf8(out, type);
                out.writeInt(data.length);
                out.write(data);
            } catch (IOException e) {
                handleError(e);
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
