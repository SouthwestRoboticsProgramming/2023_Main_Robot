package com.swrobotics.messenger.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class RemoteClient implements Client, Runnable {
    private static final String HEARTBEAT = "_Heartbeat";
    private static final String LISTEN = "_Listen";
    private static final String UNLISTEN = "_Unlisten";
    private static final String DISCONNECT = "_Disconnect";

    private static final long TIMEOUT = 5000; // Max time in milliseconds between heartbeats

    private final Queue<Message> outgoingMessages;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Set<String> listening;
    private final Set<String> wildcards;

    private boolean connected = true;
    private String name = "[Unknown]";
    private boolean identified = false;
    private long lastHeartbeatTime;

    public RemoteClient(Socket socket) throws IOException {
        outgoingMessages = new ConcurrentLinkedQueue<>();
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        listening = Collections.synchronizedSet(new HashSet<>());
        wildcards = Collections.synchronizedSet(new HashSet<>());
        lastHeartbeatTime = System.currentTimeMillis();
    }

    private void readMessage() throws IOException {
//        System.out.println("Reading message type");
        String type = in.readUTF();

        int dataSz = in.readInt();
//        System.out.println("Type " + type + ", reading " + dataSz + " data bytes");
        byte[] data = new byte[dataSz];
        in.readFully(data);

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        switch (type) {
            case HEARTBEAT: {
                lastHeartbeatTime = System.currentTimeMillis();
                break;
            }
            case LISTEN: {
                String listenType = in.readUTF();
                System.out.println("Client " + name + " listening to " + listenType);
                MessengerServer.get().broadcastEvent("Listen", name, listenType);
                if (listenType.charAt(listenType.length() - 1) == '*') {
                    wildcards.add(listenType.substring(0, listenType.length() - 1));
                } else {
                    listening.add(listenType);
                }
                break;
            }
            case UNLISTEN: {
                String unlistenType = in.readUTF();
                System.out.println("Client " + name + " no longer listening to " + unlistenType);
                MessengerServer.get().broadcastEvent("Unlisten", name, unlistenType);
                listening.remove(unlistenType);
                wildcards.remove(unlistenType.substring(0, unlistenType.length() - 1));
                break;
            }
            case DISCONNECT: {
                connected = false;
                System.out.println("Client " + name + " disconnected");
                MessengerServer.get().broadcastEvent("Disconnect", name, "");
                break;
            }
            default: {
                MessengerServer.get().onMessage(new Message(type, data));
                break;
            }
        }
    }

    private void writeMessage(Message msg) throws IOException {
        out.writeUTF(msg.getType());

        byte[] data = msg.getData();
        out.writeInt(data.length);
        out.write(data);
    }

    @Override
    public void run() {
        MessengerServer.get().addClient(this);

        try {
            main: while (connected) {
                // Check timeout
                if (System.currentTimeMillis() - lastHeartbeatTime > TIMEOUT) {
                    System.out.println("Client " + name + " disconnected due to heartbeat timeout");

                    MessengerServer.get().broadcastEvent("Timeout", name, "");
                    break;
                }

                // Read incoming messages
                while (in.available() > 0) {
                    if (identified) {
                        readMessage();

                        if (!connected) {
                            break main;
                        }
                    } else {
                        name = in.readUTF();
                        identified = true;

                        MessengerServer.get().broadcastEvent("Connect", name, "");
                    }
                }

                // Send outgoing messages
                Message msg;
                while ((msg = outgoingMessages.poll()) != null) {
                    writeMessage(msg);
                }

                // Give other threads a chance to run
                try {
                    Thread.sleep(1000 / 50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Disconnect socket if not already
            if (!socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.err.println("Exception in remote client connection " + name + ":");
            e.printStackTrace();

            MessengerServer.get().broadcastEvent("Error", name, "");
        }

        MessengerServer.get().removeClient(this);
    }

    @Override
    public void sendMessage(Message msg) {
        outgoingMessages.add(msg);
    }

    @Override
    public boolean listensTo(String type) {
        if (listening.contains(type))
            return true;

        for (String wildcard : wildcards) {
            if (type.startsWith(wildcard)) {
                return true;
            }
        }

        return false;
    }
}
