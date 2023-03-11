package com.swrobotics.robot.input;

import com.swrobotics.lib.input.XboxController;
import com.swrobotics.lib.swerve.commands.PathfindToPointCommand;
import com.swrobotics.lib.swerve.commands.TurnToAngleCommand;
import com.swrobotics.mathlib.*;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.commands.BalanceSequenceCommand;
import com.swrobotics.robot.commands.LimelightAutoAimCommand;
import com.swrobotics.robot.positions.SnapPositions;
import com.swrobotics.robot.subsystems.intake.GamePiece;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public final class Input extends SubsystemBase {
    /* 
     * left bumper:   set to cube
     * right bumper:  set to cone
     * left trigger:  eject
     * a:             floor pickup
     * b:             shelf pickup
     * dpad up:       top score
     * dpad down:     mid score
     * analog sticks: arm nudge
     * 
     * x pressed: default arm
     */

    public enum IntakeMode {
        INTAKE, EJECT, OFF
    }


    // FIXME: NULL = Smelly
    LimelightAutoAimCommand limelightAutoAimCommand = null;

    private static final int DRIVER_PORT = 0;
    private static final int MANIPULATOR_PORT = 1;

    private static final double DEADBAND = 0.1;

    private static final double SLOW_MODE_MULTIPLIER = 0.5;
    private static final double FAST_SPEED = 4.11;
    private static final Angle MAX_ROTATION = AbsoluteAngle.rad(Math.PI);

    private static final double NUDGE_PER_PERIODIC = 0.25 * 0.02;

    private final RobotContainer robot;
	
	
	private Translation2d currentSnapPosition = SnapPositions.DEFAULT;

    private final XboxController driver;
    private final XboxController manipulator;

    private final PathfindToPointCommand snapDriveCmd;
    private final TurnToAngleCommand snapTurnCmd;
    private Angle snapAngle;

    private Translation2d prevArmTarget;
    private Translation2d armNudge;

    private double lastSpeed = 0;
    private GamePiece gamePiece;

    public Input(RobotContainer robot) {
        this.robot = robot;

        driver = new XboxController(DRIVER_PORT);
        manipulator = new XboxController(MANIPULATOR_PORT);

        driver.back.onRising(robot.drivetrainSubsystem::zeroGyroscope);
        driver.start.onRising(new BalanceSequenceCommand(robot, false));

        manipulator.leftBumper.onRising(() -> gamePiece = GamePiece.CUBE);
        manipulator.rightBumper.onRising(() -> gamePiece = GamePiece.CONE);

        snapDriveCmd = new PathfindToPointCommand(robot, null);
        snapTurnCmd = new TurnToAngleCommand(robot, () -> snapAngle, false);

        prevArmTarget = SnapPositions.DEFAULT;
        armNudge = new Translation2d(0, 0);
        gamePiece = GamePiece.CUBE;
    }

    private double deadband(double val) {
        return MathUtil.deadband(val, DEADBAND);
    }

    // ---- Driver controls ----

    public Vec2d getDriveTranslation() {
//        boolean slowMode = driver.leftBumper.isPressed();
        boolean fastMode = driver.rightBumper.isPressed();

        double speed = 1.5;
//        if (slowMode)
//            multiplier *= SLOW_MODE_MULTIPLIER;
        if (fastMode) {
            speed = FAST_SPEED;
        }

        if (speed != lastSpeed) {
            speed = (speed + lastSpeed) / 2;
            // FIXME: Accual decceleration
        }

        lastSpeed = speed;

        double x = -deadband(driver.leftStickY.get()) * speed;
        double y = -deadband(driver.leftStickX.get()) * speed;

        return new Vec2d(x, y);
    }

    public Angle getDriveRotation() {
        return MAX_ROTATION.cw().mul(deadband(driver.rightStickX.get()));
    }

    public boolean isRobotRelative() {
        return driver.rightTrigger.get() > 0.8;
    }

    private void driverPeriodic() {
        if (driver.leftBumper.isPressed()) {
            SnapPositions.SnapStatus snap = SnapPositions.getSnap(robot.drivetrainSubsystem.getPose());
            snapToPosition(snap.snapPosition);
            snapToAngle(snap.snapRotation);

            boolean driveInput = Math.abs(driver.leftStickX.get()) > DEADBAND || Math.abs(driver.leftStickY.get()) > DEADBAND;
            boolean turnInput = Math.abs(driver.rightStickX.get()) > DEADBAND;

            boolean rumble = (driveInput && snap.snapPosition != null) || (turnInput && snap.snapRotation != null);
            driver.setRumble(rumble ? 1 : 0);
        } else {
            setCommandEnabled(snapDriveCmd, false);
            setCommandEnabled(snapTurnCmd, false);
            driver.setRumble(0);
        }

        if(driver.b.isFalling()) {
            limelightAutoAimCommand = new LimelightAutoAimCommand(robot.drivetrainSubsystem, robot.limelight, 0);
            limelightAutoAimCommand.schedule();
        } else if (driver.b.isRising()) {
            if (limelightAutoAimCommand.isScheduled()) {
                limelightAutoAimCommand.cancel();
            }
            limelightAutoAimCommand = null;
        }
    }

    // ---- Manipulator controls ----

    private GamePiece getGamePiece() {
        return gamePiece;
    }

    private boolean isEject() {
        return manipulator.leftTrigger.get() > 0.8;
    }
	

    public Translation2d getArmTarget() {
        if (manipulator.dpad.up.isPressed())
			currentSnapPosition = getArmHigh();
        if (manipulator.dpad.down.isPressed())
			currentSnapPosition = getArmMid();
        if (manipulator.b.isPressed())
			currentSnapPosition = getSubstationPickup();
        if (manipulator.a.isPressed())
            currentSnapPosition = robot.arm.getHomeTarget();
		if (manipulator.x.isPressed())
			currentSnapPosition = SnapPositions.DEFAULT;


        return currentSnapPosition;
    }

    private Translation2d getArmHigh() {
        if (getGamePiece() == GamePiece.CUBE)
            return SnapPositions.CUBE_UPPER;
        return SnapPositions.CONE_UPPER;
    }

    private Translation2d getArmMid() {
        if (getGamePiece() == GamePiece.CUBE)
            return SnapPositions.CUBE_CENTER;
        return SnapPositions.CONE_CENTER;
    }

    private Translation2d getSubstationPickup() {
        if (getGamePiece() == GamePiece.CUBE)
            return SnapPositions.CUBE_PICKUP;
        return SnapPositions.CONE_PICKUP;
    }

    private IntakeMode getIntakeMode() {
        if (isEject())
            return IntakeMode.EJECT;

        if (manipulator.a.isPressed() || manipulator.b.isPressed())
            return IntakeMode.INTAKE;

        return IntakeMode.OFF;
    }

    private void setCommandEnabled(Command cmd, boolean enabled) {
        if (enabled && !cmd.isScheduled())
            cmd.schedule();
        if (!enabled && cmd.isScheduled())
            cmd.cancel();
    }

    private void snapToPosition(Translation2d position) {
        if (position != null)
            snapDriveCmd.setGoal(new Vec2d(position.getX(), position.getY()));

        setCommandEnabled(snapDriveCmd, position != null);
    }

    private void snapToAngle(Rotation2d angle) {
        if (angle != null)
            snapAngle = CCWAngle.rad(angle.getRadians());

        setCommandEnabled(snapTurnCmd, angle != null);
    }

    private void manipulatorPeriodic() {
        IntakeMode intakeMode = getIntakeMode();
        switch (intakeMode) {
            case INTAKE:
                robot.intake.setExpectedPiece(getGamePiece());
                robot.intake.run();
                break;
            case EJECT:
                robot.intake.eject();
                break;
            case OFF:
                robot.intake.stop();
                break;
        }

        // Update arm nudge
        armNudge = armNudge.plus(new Translation2d(
            deadband(manipulator.rightStickX.get()) * NUDGE_PER_PERIODIC,
            deadband(-manipulator.rightStickY.get()) * NUDGE_PER_PERIODIC
            ));

        Translation2d armTarget = getArmTarget();


        // If it is moving to a new target
        if (!armTarget.equals(prevArmTarget)) {
            armNudge = new Translation2d(0, 0);

            // Move to an intermediate position first
            robot.arm.setTargetPosition(new Translation2d(0.6, Math.max(armTarget.getY(), prevArmTarget.getY())));
            if (!robot.arm.isInTolerance()) {
                return; // Keep moving to the intermediate position
            }
        }
                
        robot.arm.setTargetPosition(getArmTarget().plus(armNudge));
        prevArmTarget = armTarget;

        armNudge = armNudge.plus(new Translation2d(
                deadband(manipulator.leftStickX.get()) * NUDGE_PER_PERIODIC,
                deadband(-manipulator.leftStickY.get()) * NUDGE_PER_PERIODIC
        ));

        robot.arm.setTargetPosition(armTarget.plus(armNudge));
    }

    @Override
    public void periodic() {
        if (!DriverStation.isTeleop())
            return;

        driverPeriodic();
        manipulatorPeriodic();
    }
}
