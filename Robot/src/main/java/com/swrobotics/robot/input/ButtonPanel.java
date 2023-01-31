package com.swrobotics.robot.input;

import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public final class ButtonPanel extends SubsystemBase {
    private static final String MSG_BUTTON_DATA = "ButtonPanel:Buttons";
    private static final String MSG_LIGHT_DATA = "ButtonPanel:Lights";

    public static final int WIDTH = 9;
    public static final int HEIGHT = 4;

    private final MessengerClient msg;

    private final boolean[] buttonStates;
    private final boolean[] lightStates;

    public ButtonPanel(MessengerClient msg) {
        this.msg = msg;

        buttonStates = new boolean[WIDTH * HEIGHT];
        lightStates = new boolean[WIDTH * HEIGHT];

        msg.addHandler(MSG_BUTTON_DATA, this::onButtonData);
    }

    public boolean isButtonDown(int x, int y) {
        return buttonStates[x + y * WIDTH];
    }

    public void setLightOn(int x, int y, boolean on) {
        lightStates[x + y * WIDTH] = on;
    }

    private void onButtonData(String type, MessageReader reader) {
        byte[] packed = reader.readRaw(5);
        for (int i = 0; i < packed.length; i++) {
            for (int j = 0; j < (i == WIDTH - 1 ? 8 : 4); j++) {
                int x = i * 2 + j / 4;
                int y = j % 4;
                buttonStates[x + y * WIDTH] = (packed[i] & (1 << j)) != 0;
            }
        }
    }

    @Override
    public void periodic() {
        byte[] packed = new byte[5];
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 4; y++) {
                byte bit = (byte) (1 << (y + (x % 2) * 4));
                if (lightStates[x + y * WIDTH])
                    packed[x / 2] |= bit;
            }
        }

        msg.prepare(MSG_LIGHT_DATA)
                .addRaw(packed)
                .send();
    }
}
