package com.swrobotics.messenger.test;

import com.swrobotics.messenger.client.MessengerClient;

public final class MessengerTest {
    private static void sleepWithRead(MessengerClient msg, int iterations) {
        for (int i = 0; i < iterations; i++) {
            System.out.println("Reading");
            msg.readMessages();

            System.out.println("Sleeping");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    public static void main(String[] args) {
        MessengerClient msg = new MessengerClient("localhost", 5805, "Test");

        while (!msg.isConnected()) {
            Thread.onSpinWait();
        }

        msg.addHandler("Test", (type, reader) -> {
            System.out.println("Direct: " + reader.readInt());
        });
        msg.addHandler("Test2*", (type, reader) -> {
            System.out.println("Wildcard: " + reader.readInt());
        });

        int i = 0;
        while (true) {
            System.out.println("Writing");
            msg.prepare("Test")
                    .addInt(i++)
                    .send();

            sleepWithRead(msg, 1);
        }

//        for (int i = 0; i < 5; i++) {
//            msg.prepare("Test")
//                    .addInt(i)
//                    .send();
//
//            sleepWithRead(msg, 10);
//        }
//
//        for (int i = 0; i < 5; i++) {
//            msg.prepare("Test2" + Math.random())
//                    .addInt(i)
//                    .send();
//
//            sleepWithRead(msg, 10);
//        }
//
//        for (int i = 0; i < 100; i++) {
//            msg.prepare("FastTest")
//                    .addInt(i)
//                    .send();
//
//            sleepWithRead(msg, 1);
//        }

       // msg.disconnect();
    }
}
