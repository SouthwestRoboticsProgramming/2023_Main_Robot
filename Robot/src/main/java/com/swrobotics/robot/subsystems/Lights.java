package com.swrobotics.robot.subsystems;

import edu.wpi.first.wpilibj.motorcontrol.Spark;

public class Lights {

    public enum Color {

        RAINBOW(-0.99),
        RAINBOW_PARTY(-0.97),
        RAINBOW_OCEAN(-0.95),
        RAINBOW_LAVA(-0.93),
        RAINBOW_FOREST(-0.91),
        RAINBOW_GLITTER(-0.89),
        CONFETTI(-0.87),
        SHOT_RED(-0.85),
        SHOT_BLUE(-0.83),
        SHOT_WHITE(-0.81),
        SINELON_RAINBOW(-0.79),
        SINELON_PARTY(-0.77),
        SINELON_OCEAN(-0.75),
        SINELON_LAVA(-0.73),
        SINELON_FOREST(-0.71),
        BPM_RAINBOW(-0.69),
        BPM_PARTY(-0.67),
        BPM_OCEAN(-0.65),
        BPM_LAVA(-0.63),
        BPM_FOREST(-0.61),
        FIRE_MEDIUM(-0.59),
        FIRE_LARGE(-0.57),
        TWINKLES_RAINBOW(-0.55),
        TWINKLES_PARTY(-0.53),
        TWINKLES_OCEAN(-0.51),
        TWINKLES_LAVA(-0.49),
        TWINKLES_FOREST(-0.47),
        WAVES_RAINBOW(-0.45),
        WAVES_PARTY(-0.43),
        WAVES_OCEAN(-0.41),
        WAVES_LAVA(-0.39),
        WAVES_FOREST(-0.37),
        SCANNER_RED(-0.35),
        SCANNER_GRAY(-0.33),
        CHASE_RED(-0.31),
        CASE_BLUE(-0.29),
        CHASE_GRAY(-0.27),
        HEARTBEAT_RED(-0.25),
        HEARTBEAT_BLUE(-0.23),
        HEARTBEAT_WHITE(-0.21),
        HEARTBEAT_GRAY(-0.19),
        BREATH_RED(-0.17),
        BREATH_BLUE(-0.15),
        BREATH_GRAY(-0.13),
        STROBE_RED(-0.11),
        STROBE_BLUE(-0.09),
        STROBE_GOLD(-0.07),
        STROBE_WHITE(-0.05),

        OFF(0.0),
        // TODO: Custom patterns
        HOT_PINK(0.57),
        DARK_RED(0.59),
        RED(0.61),
        RED_ORANGE(0.63),
        ORANGE(0.65),
        GOLD(0.67),
        YELLOW(0.69),
        LAWN_GREEN(0.71),
        LIME(0.73),
        DARK_GREEN(0.75),
        GREEN(0.77),
        BLUE_GREEN(0.79),
        AQUA(0.81),
        SKY_BLUE(0.83),
        DARK_BLUE(0.85),
        BLUE(0.87),
        BLUE_VIOLET(0.89),
        VIOLET(0.91),
        WHITE(0.93),
        GRAY(0.95),
        DARK_GRAY(0.97),
        BLACK(0.99),

        /** Do not set the lights to this value, they won't do anything */
        UNKNOWN(0.0);



        private final double value;

        private Color(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }

    /**
     * Allows constants to be set up for consistent colors across subsystems
     */
    public enum IndicatorMode {
        OFF(Color.BLACK, 0),
        IN_PROGRESS(Color.GOLD, 1),
        GOOD(Color.DARK_GREEN, 2),
        SUCCESS(Color.GREEN, 3),
        FAILED(Color.RED, 3),
        CRITICAL_FAILED(Color.STROBE_RED, 4);

        private final Color color;
        private final int severity;

        private IndicatorMode(Color color, int severity) {
            this.color = color;
            this.severity = severity;
        }

        public Color getColor() {
            return color;
        }

        public int getSeverity() {
            return severity;
        }
    }

    private final Spark lights = new Spark(0); // The REV Blinkin is treated like a spark max

    private double currentOutput;
    private Color currentColor = Color.OFF;
    private IndicatorMode currentMode = IndicatorMode.OFF;

    public void set(double output) {
        lights.set(output);

        currentOutput = output;
        currentColor = Color.UNKNOWN;
        currentMode = IndicatorMode.OFF;
    }

    /**
     * Set the color of the lights without respect for the color already set
     * Use this when trying to make the robot look cool. If you are indicating something, use IndicatorMode instead.
     * @param color
     */
    public void set(Color color) {
        lights.set(color.getValue());

        currentOutput = color.getValue();
        currentColor = color;
        currentMode = IndicatorMode.OFF;
    }

    public void set(IndicatorMode mode) {
        // The mode must be of higher importance to be set
        if (mode.getSeverity() >= getMode().getSeverity()) {
            set(mode.getColor());
        }

        currentOutput = mode.getColor().getValue();
        currentColor = mode.getColor();
        currentMode = mode;
    }
    public void setbyseverity(int severity) {
        if (severity >= 5) {
            lights.set(Color.GREEN.value);

        }
        else if (severity == 4) {
            lights.set(Color.DARK_GREEN.value);

        }
        else if (severity == 3) {
            lights.set(Color.YELLOW.value);
        }
        else if (severity == 2) {
            lights.set(Color.ORANGE.value);
        } else if(severity == 0){
            lights.set(Color.RED.value);
        }
        else {
            lights.set(Color.STROBE_RED.value);
        }
    }

    public IndicatorMode getMode() {
        return currentMode;
    }

    public void setDebug(double value) {
        lights.set(value);
    }
}
