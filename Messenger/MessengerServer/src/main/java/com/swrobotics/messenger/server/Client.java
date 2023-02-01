package com.swrobotics.messenger.server;

public interface Client {
    String getName();

    void sendMessage(Message msg);
    boolean listensTo(String type);
}
