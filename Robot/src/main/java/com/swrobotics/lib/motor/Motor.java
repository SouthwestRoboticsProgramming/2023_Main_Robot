package com.swrobotics.lib.motor;

import java.util.function.Supplier;

import com.swrobotics.lib.schedule.Scheduler;
import com.swrobotics.lib.schedule.Subsystem;
import com.swrobotics.mathlib.MathUtil;

/**
 * Represents a generic motor. This class is intended to provide
 * a common interface between various types of motors, to make it
 * easier to swap motors and to learn how to program them.
 */
public abstract class Motor implements Subsystem {
    /**
     * Describes ways to control the motor to a target
     */
    private enum ControlMode {
        PERCENT;
    }
    
    private ControlMode controlMode;
    private Runnable runOnPeriodic;

    private boolean inverted = false; // If the motor and encoder should function backwards
    private double neutralDeadband = 0.01; // All outputs below this value are assumed to be zero
    private double nominalOutput = 0.0; // All outputs below this value, but above the neutral deadband are upgraded to this value
    private double maxVoltage = 12.0;

     /** Current output of the motor in percent (Updated by {@code percentFiltered()}) */
    private double currentOutput = 0.0;

    /**
     * Creates a new {@code Motor} instance that belongs to a specified {@link Subsystem}.
     * 
     * @param parent parent subsystem
     */
    public Motor(Subsystem parent) {
        /* Schedule it as a subsystem so that it can continue to target a setpoint,
        even not when being called */
        Scheduler.get().addSubsystem(parent, this);

        percent(0);
    }

    /**
     * Sets the percent output power of the motor. A negative percent
     * value will cause the motor to turn in the opposite direction.
     * 
     * @param percent percent output from -1 to 1
     */
    public void percent(double percent) {
        controlMode = ControlMode.PERCENT;
        runOnPeriodic = () -> percentFiltered(percent); // Keep going to percent out, even when this is not called
    }

    public void percent(Supplier<Double> percent) {
        percent(() -> percent.get());
    }

    /**
     * Stops the motor by setting its percent output to zero. This
     * completely disables the motor output.
     */
    public void stop() { percent(0); }

    /**
     * Gets whether the motor's output is currently inverted.
     * 
     * @return inverted
     */
    public boolean getInverted() {
        return inverted;
    }

    /**
     * Sets whether the motor and sensor's output should be inverted.
     * 
     * @param inverted whether to invert output
     */
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    /**
     * Gets the currently set neutral deadband.
     * 
     * @return Neutral deadband
     */
    public double getNeutralDeadband() {
        return neutralDeadband;
    }

    /**
     * Sets the range at which the motor will assume the percent output
     * is neutral. If the percent output is within the deadband, it will
     * be clamped to zero.
     * 
     * @param neutralDeadband New neutral deadband
     */
    public void setNeutralDeadband(double neutralDeadband) {
        this.neutralDeadband = neutralDeadband;
    }

    /**
     * Gets the currently set nominal output. All demands under this value, but over the neutral deadband will be upgraded to this value.
     * 
     * @return Nominal output
     */
    public double getNominalOutput() {
        return nominalOutput;
    }

    /**
     * Sets the value that very small demanded motor outputs are upgraded to.
     * If the demand is less than the nominal output but greater than 
     * the neutral deadband, it will be upgraded to the nominal output.
     * 
     * @param nominalOutput New nominal output
     */
    public void setNominalOutput(double nominalOutput) {
        this.nominalOutput = nominalOutput;
    }

    /**
     * Get the currently set maximum voltage. No demands will exceed this value.
     * @return
     */
    public double getMaximumVoltage() {
        return maxVoltage;
    }

    /**
     * Set the maximum voltage that {@code percent()} is allowed to run at.
     * @param maxVoltage
     */
    public void setMaximumVoltage(double maxVoltage) {
        this.maxVoltage = maxVoltage;
    }

    /**
     * Get the mode that the motor is currently using
     * @return Mode that the motor is currently using
     */
    public ControlMode getControlMode() {
        return controlMode;
    }

    /**
     * Get the percentage of max power that the motor is currently being sent
     * @return Percentage of maximum power currently being sent
     */
    public double getCurrentPercentOutput() {
        return currentOutput;
    }

    /**
     * Actually sets the motor's voltage output. This should be impolemented
     * by all motor types to be able to control them. A voltage output of 12
     * should be full speed clockwise, a voltage output of -12 should be full
     * speed counterclockwise, and a voltage output of 0 should be stopped.
     * @param voltage
     */
    protected abstract void  setVoltageInternal(double voltage);

    /* Filtering and setting output */

    private void percentFiltered(double percent) {
        if (inverted) percent = -percent;

        // Apply neutral deadband
        percent = MathUtil.deadband(percent, neutralDeadband);
        
        // Apply nominal output
        if (percent != 0 && Math.abs(percent) < nominalOutput) {
            percent = Math.signum(percent) * nominalOutput;
        }

        // Clamp output
        percent = MathUtil.clamp(percent, -1, 1);

        currentOutput = percent;

        // Apply voltage
        double voltage = percent * maxVoltage;
        
        setVoltageInternal(voltage);
    }

    @Override
    public void periodic() {
        // Run the currently set control mode implementation
        runOnPeriodic.run();
    }
}
