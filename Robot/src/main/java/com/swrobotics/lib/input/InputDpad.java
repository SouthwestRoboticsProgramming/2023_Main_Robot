package com.swrobotics.lib.input;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CWAngle;

import java.util.function.Supplier;

/**
 * Represents a D-pad (POV) input on a controller. A D-pad can act as four independent buttons, two
 * axes, or an angle input.
 */
public final class InputDpad implements InputElement {
    private final Supplier<Integer> getter;
    public final InputButton up, down, left, right;
    public final InputAxis vertical, horizontal;

    private int angleDeg;
    private Angle angle;

    /**
     * Creates a new D-pad input that reads its value from a provided getter function. The getter
     * should return a clockwise angle in degrees, with up being zero, and return -1 when no buttons
     * are pressed, as the WPILib POV input does.
     *
     * @param getter value getter
     */
    public InputDpad(Supplier<Integer> getter) {
        this.getter = getter;

        angleDeg = getter.get();
        angle = calcAngle();

        up = new InputButton(() -> angleDeg == 0 || angleDeg == 45 || angleDeg == 315);
        down = new InputButton(() -> angleDeg == 135 || angleDeg == 180 || angleDeg == 225);
        left = new InputButton(() -> angleDeg == 45 || angleDeg == 90 || angleDeg == 135);
        right = new InputButton(() -> angleDeg == 225 || angleDeg == 270 || angleDeg == 315);

        vertical = new InputAxis(() -> up.isPressed() ? 1.0 : (down.isPressed() ? -1.0 : 0.0));
        horizontal = new InputAxis(() -> right.isPressed() ? 1.0 : (left.isPressed() ? -1.0 : 0.0));
    }

    /**
     * Gets whether any button on the D-pad is pressed.
     *
     * @return pressed
     */
    public boolean isPressed() {
        return angleDeg >= 0;
    }

    /**
     * Gets the angle that the D-pad is currently pressed. Zero represents up.
     *
     * @return angle
     */
    public Angle getAngle() {
        return angle;
    }

    // Calculates the angle based on the POV measurement
    private Angle calcAngle() {
        return isPressed() ? CWAngle.deg(angleDeg) : Angle.ZERO;
    }

    @Override
    public void update() {
        up.update();
        down.update();
        left.update();
        right.update();

        vertical.update();
        horizontal.update();

        angleDeg = getter.get();
        angle = calcAngle();
    }
}
