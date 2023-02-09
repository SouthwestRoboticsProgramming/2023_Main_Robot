package com.swrobotics.shufflelog.tool.buttons;

import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.util.Cooldown;
import imgui.ImGui;

import java.util.Arrays;

public final class RobotButtonIO {
    private static final String MSG_BUTTON_DATA = "ButtonPanel:Buttons";
    private static final String MSG_LIGHT_DATA = "ButtonPanel:Lights";

    // Match robot periodic per second
    private static final int UPDATES_PER_SECOND = 50;

    private final MessengerClient msg;
    private final ButtonPanel panel;
    private final Cooldown updateCooldown;

    private boolean enabled;

    public RobotButtonIO(MessengerClient msg, ButtonPanel panel) {
        this.msg = msg;
        this.panel = panel;
        updateCooldown = new Cooldown(1_000_000_000L / UPDATES_PER_SECOND);

        msg.addHandler(MSG_LIGHT_DATA, this::onLightData);

        enabled = true;
    }

    public RobotButtonIO setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    private void onLightData(String type, MessageReader reader) {
        if (!enabled) return;

        byte[] packed = reader.readRaw(5);
        for (int i = 0; i < packed.length; i++) {
            for (int j = 0; j < (i == 4 ? 4 : 8); j++) {
                int x = i * 2 + j / 4;
                int y = j % 4;
                panel.setButtonLight(x, y, (packed[i] & (1 << j)) != 0);
            }
        }
    }

    public void sendButtonData() {
        if (!enabled || !updateCooldown.request())
            return;

        byte[] packed = new byte[5];
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 4; y++) {
                byte bit = (byte) (1 << (y + (x % 2) * 4));
                if (panel.isButtonDown(x, y))
                    packed[x / 2] |= bit;
            }
        }

        msg.prepare(MSG_BUTTON_DATA)
                .addRaw(packed)
                .send();
    }
}
