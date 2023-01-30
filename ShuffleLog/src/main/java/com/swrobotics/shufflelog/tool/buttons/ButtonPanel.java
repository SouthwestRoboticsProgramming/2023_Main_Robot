package com.swrobotics.shufflelog.tool.buttons;

public interface ButtonPanel {
    enum SwitchState {
        UP, DOWN
    }

    int WIDTH = 9;
    int HEIGHT = 4;

    boolean isButtonDown(int x, int y);
    void setButtonLight(int x, int y, boolean on);

    SwitchState getSwitchState();

    boolean isConnected();
    void processIO();
}
