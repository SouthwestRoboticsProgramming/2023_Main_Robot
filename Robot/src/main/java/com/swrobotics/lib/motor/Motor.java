package com.swrobotics.lib.motor;

import java.util.function.Supplier;

import com.swrobotics.lib.schedule.Scheduler;
import com.swrobotics.lib.schedule.Subsystem;

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
        PERCENT,
        VOLTAGE;
    }
    
    private ControlMode controlMode;

    private boolean inverted; // If the motor and encoder should function backwards
    private double neutralDeadband; // All outputs below this value are assumed to be zero
    private double nominalOutput; // All outputs below this value, but above the neutral deadband are upgraded to this value

    /**
     * Creates a new {@code Motor} instance that belongs to a specified {@link Subsystem}.
     * 
     * @param parent parent subsystem
     */
    public Motor(Subsystem parent) {
        /* Schedule it as a subsystem so that it can continue to target a setpoint,
        even not when being called */
        Scheduler.get().addSubsystem(parent, this);

        inverted = false;
        neutralDeadband = 0.01;
        nominalOutput = 0.0;

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
        controlModeImpl = () -> percentImpl(percent);
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
     * Actually sets the motor's percent output. This should be implemented
     * by all motor types to be able to control them. A percent output of 1
     * should be full speed clockwise, a percent output of -1 should be full
     * speed counterclockwise, and a percent output of 0 should be stopped.
     * The provided percent output will never exceed this range.
     * 
     * @param percent percent output from -1 to 1
     */
    protected abstract void setPercentOutInternal(double percent);

    private void setPercentOutFiltered(double percent) {
        if (inverted) percent = -percent;

        // Apply neutral deadband
        percent = MathUtil.deadband(percent, neutralDeadband);
        
        // Apply nominal output
        if (percent != 0 && Math.abs(percent) < nominalOutput) {
            percent = Math.signum(percent) * nominalOutput;
        }
        
        setPercentOutInternal(MathUtil.clamp(percent, -1, 1));
    }

    // Implementation of percent output control mode
    private void percentImpl(double percent) {
        setPercentOutFiltered(percent);
    }

    // Implementation of position control mode (used for position() and hold())
    private void positionImpl(Supplier<Angle> angleGetter, Angle angle) {
        setPercentOutFiltered(positionCalc.calculate(angleGetter.get(), angle));
    }

    // Implementation of velocity control mode (used for velocity() and halt())
    private void velocityImpl(Supplier<Angle> velocityGetter, Angle velocity) {
        setPercentOutFiltered(velocityCalc.calculate(velocityGetter.get(), velocity));
    }

    @Override
    public void periodic() {
        // Run the currently set control mode implementation
        controlModeImpl.run();
    }
}
