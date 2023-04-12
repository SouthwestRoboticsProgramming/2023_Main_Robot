package com.swrobotics.messenger.test;

import com.swrobotics.messenger.client.MessengerClient;

public class MessengerTest2 {
  public static void main(String[] args) throws Exception {
    MessengerClient client = new MessengerClient("localhost", 5805, "TestClient");
    client.addHandler(
        "Test",
        (type, reader) -> {
          System.out.println("Received " + type + " (" + reader.readInt() + ")");
        });

    for (int i = 0; i < 100; i++) {
      client.prepare("Test").addInt(i).send();
      System.out.println("Sent " + i);

      for (int j = 0; j < 10; j++) {
        client.readMessages();
        Thread.sleep(100);
      }
    }
    client.disconnect();
  }
}
