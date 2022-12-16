package com.swrobotics.robot.subsystems;

import edu.wpi.first.wpilibj.motorcontrol.Spark;

public class Lights {

    public enum Color {

        OFF(0.0),
        PINK(0.57),
        RED(0.61),
        GOLD(0.67),
        GREEN(0.77),
        BLUE(0.87),
        WHITE(0.93),
        CONFETTI(-0.87),
        RAINBOW(-0.99);

        private final double value;

        private Color(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }

    private final Spark lights = new Spark(0); // The REV Blinkin is treated like a spark max

    public void setColor(Color color) {
        lights.set(color.getValue());
    }
}
