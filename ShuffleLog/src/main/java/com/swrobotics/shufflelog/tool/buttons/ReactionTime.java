package com.swrobotics.shufflelog.tool.buttons;

import com.swrobotics.shufflelog.util.Cooldown;

import java.util.ArrayList;
import java.util.List;

public final class ReactionTime {
    private static final class Position {
        private final int x, y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private final List<Position> remainingPositions;
    private final ButtonPanel panel;
    private final Cooldown nextLightCooldown;
    private Position choice;

    private boolean prevWasDown;
    private boolean hasPressedChoice;

    public ReactionTime(ButtonPanel panel) {
        this.panel = panel;
        remainingPositions = new ArrayList<>();
        nextLightCooldown = new Cooldown(500_000_000L);
    }

    public void begin() {
        for (int y = 0; y < ButtonPanel.HEIGHT; y++) {
            for (int x = 0; x < ButtonPanel.WIDTH; x++) {
                panel.setButtonLight(x, y, false);

                if (y != 3 || (x != 4 && x != 5))
                    remainingPositions.add(new Position(x, y));
            }
        }

        choice = null;
    }

    private void chooseNewPoint() {
        choice = remainingPositions.get((int) (Math.random() * remainingPositions.size()));
        hasPressedChoice = false;
        prevWasDown = panel.isButtonDown(choice.x, choice.y);
    }

    public void update() {
        if (choice != null) {
            boolean down = panel.isButtonDown(choice.x, choice.y);
            if (down && !prevWasDown)
                hasPressedChoice = true;
            prevWasDown = down;
        }

        if (nextLightCooldown.request()) {
            if (remainingPositions.isEmpty()) {
                begin();
                return;
            }

            if (choice != null) {
                if (!hasPressedChoice)
                    panel.setButtonLight(choice.x, choice.y, false);
                else
                    remainingPositions.remove(choice);
            }

            chooseNewPoint();
            panel.setButtonLight(choice.x, choice.y, true);
        }
    }
}
