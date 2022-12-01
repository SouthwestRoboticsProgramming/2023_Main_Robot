package com.swrobotics.messenger.server;

public final class MessengerServerMain {
    public static void main(String[] args) {
        RemoteClientConnector conn = new RemoteClientConnector();
        conn.run();
    }

    private MessengerServerMain() {
        throw new AssertionError();
    }
}
