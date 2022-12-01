package com.swrobotics.messenger.server;

public interface Client {
    void sendMessage(Message msg);
    boolean listensTo(String type);
}
