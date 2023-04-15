package com.swrobotics.messenger.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class RemoteClientConnector implements Runnable {
    public RemoteClientConnector() {}

    @Override
    public void run() {
        int port = MessengerServer.get().getConfig().getPort();

        System.out.println("Opening port " + port + " for Messenger");

        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Failed to open port " + port);
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Listening for incoming connections");

        while (true) {
            try {
                Socket clientSocket = socket.accept();
                System.out.println("Client has connected");

                RemoteClient client = new RemoteClient(clientSocket);
                new Thread(client).start();
            } catch (IOException e) {
                System.err.println("Exception whilst accepting a client connection:");
                e.printStackTrace();
            }
        }
    }
}
