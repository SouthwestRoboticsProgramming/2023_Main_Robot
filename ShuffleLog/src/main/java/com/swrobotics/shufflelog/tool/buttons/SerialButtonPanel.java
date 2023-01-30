package com.swrobotics.shufflelog.tool.buttons;

import com.fazecast.jSerialComm.SerialPort;
import com.swrobotics.shufflelog.util.Cooldown;
import imgui.ImGui;

import java.util.Arrays;

/**
 * Implementation of the button panel that communicates
 * with the physical button panel over serial.
 */
public final class SerialButtonPanel implements ButtonPanel {
    private static final class Button {
        private final int group;
        private final byte bit;

        public Button(int group, int bitIdx) {
            this.group = group;
            bit = (byte) (1 << bitIdx);
        }
    }

    // Indexed BUTTONS[x][y]
    // Mapping from button locations to data bits
    private static final Button[][] BUTTONS = {
            {new Button(0, 3), new Button(0, 2), new Button(0, 1), new Button(0, 0)},
            {new Button(1, 3), new Button(1, 2), new Button(1, 1), new Button(1, 0)},
            {new Button(2, 3), new Button(2, 2), new Button(2, 1), new Button(2, 0)},
            {new Button(3, 3), new Button(3, 2), new Button(3, 1), new Button(3, 0)},
            {new Button(4, 3), new Button(4, 2), new Button(4, 1), new Button(4, 0)},
            {new Button(5, 3), new Button(5, 2), new Button(5, 1), new Button(5, 0)},
            {new Button(5, 5), new Button(2, 5), new Button(5, 4), new Button(2, 4)},
            {new Button(4, 5), new Button(1, 5), new Button(4, 4), new Button(1, 4)},
            {new Button(3, 5), new Button(0, 5), new Button(3, 4), new Button(0, 4)}
    };

    private static final byte START_BYTE = (byte) 0xA5;
    private static final long LIGHT_DATA_COOLDOWN = 100_000_000L; // Nanoseconds

    private static final int GROUP_SIZE = 6;
    private static final int GROUP_COUNT = 6;
    private static final byte GROUP_MASK = (1 << GROUP_SIZE) - 1;

    private static final int SWITCH_GROUP = 0;
    private static final int SWITCH_BIT = 128;

    private SerialPort serial;
    private boolean waitingForStart;
    private final byte[] readBuf = new byte[GROUP_COUNT];
    private int readIdx;

    private final byte[] buttonStates = new byte[GROUP_COUNT];
    private final byte[] lightStates = new byte[GROUP_COUNT];
    private SwitchState switchState;

    private final Cooldown lightDataCooldown;
    private boolean needsLightUpdate;

    public SerialButtonPanel() {
        Arrays.fill(buttonStates, (byte) 0);
        Arrays.fill(lightStates, GROUP_MASK);

        lightDataCooldown = new Cooldown(LIGHT_DATA_COOLDOWN);
        serial = null;
        switchState = SwitchState.DOWN;

        needsLightUpdate = true;
    }

    @Override
    public boolean isButtonDown(int x, int y) {
        Button button = BUTTONS[x][y];
        return (buttonStates[button.group] & button.bit) != 0;
    }

    @Override
    public void setButtonLight(int x, int y, boolean on) {
        Button button = BUTTONS[x][y];

        boolean current = (lightStates[button.group] & button.bit) == 0;
        if (on == current)
            return; // Light is already set to this value, don't bother resending

        // Bits are negated in lightStates
        if (on)
            lightStates[button.group] &= ~button.bit;
        else
            lightStates[button.group] |= button.bit;

        needsLightUpdate = true;
    }

    @Override
    public SwitchState getSwitchState() {
        return switchState;
    }

    @Override
    public boolean isConnected() {
        return serial != null;
    }

    @Override
    public void processIO() {
        // If not connected, try to connect
        if (serial == null) {
            SerialPort[] availPorts = SerialPort.getCommPorts();
            if (availPorts.length == 0)
                return;

            serial = availPorts[0];
            serial.openPort();

            waitingForStart = true;
            readIdx = 0;
        }

        // Write out light data
        if (needsLightUpdate && lightDataCooldown.request()) {
            byte[] packet = new byte[GROUP_COUNT + 1];
            packet[0] = START_BYTE;
            System.arraycopy(lightStates, 0, packet, 1, GROUP_COUNT);

            serial.writeBytes(packet, packet.length);
            needsLightUpdate = false;
        }

        // Read in button data
        int avail = serial.bytesAvailable();
        if (avail <= 0)
            return;

        byte[] incoming = new byte[serial.bytesAvailable()];
        int read = serial.readBytes(incoming, incoming.length);
        for (int i = 0; i < read; i++) {
            byte b = incoming[i];

            if (waitingForStart) {
                if (b == START_BYTE) {
                    waitingForStart = false;
                    readIdx = 0;
                }
            } else {
                readBuf[readIdx++] = b;
                if (readIdx == readBuf.length) {
                    System.arraycopy(readBuf, 0, buttonStates, 0, GROUP_COUNT);
                    switchState = (readBuf[SWITCH_GROUP] & SWITCH_BIT) != 0 ? SwitchState.UP : SwitchState.DOWN;
                    waitingForStart = true;
                }
            }
        }
    }
}
