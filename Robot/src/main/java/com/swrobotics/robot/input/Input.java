package com.swrobotics.robot.input;

import com.swrobotics.lib.input.XboxController;
import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.mathlib.*;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.positions.ArmPositions;
import com.swrobotics.robot.subsystems.arm2.ArmPosition;
import com.swrobotics.robot.subsystems.intake.GamePiece;

import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public final class Input extends SubsystemBase {
    /*
     * Manipulator:
     *  left bumper:   set to cube
     *  right bumper:  set to cone
     *  left trigger:  eject
     *  a:             floor pickup
     *  b:             shelf pickup
     *  dpad up:       top score
     *  dpad down:     mid score
     *  analog sticks: arm nudge
     *
     * Driver:
     *  left stick:    drive translation
     *  right stick:   drive rotation
     *  right bumper:  fast mode
     *  left bumper:   snap
     *  start:         reset gyro
     *
     * snap:
     *   when at grid:
     *     drive to scoring position
     *     if cube node, turn to 180 degrees (aligned by apriltag)
     *     if cone node, limelight aim towards pole when close to 180 degrees
     *   when at substation:
     *     no auto drive
     *     limelight aim towards game piece when close to 0 degrees
     */

    private static final NTDouble SPEED_RATE_LIMIT = new NTDouble("Input/Speed Slew Limit", 20);

    public enum IntakeMode {
        INTAKE,
        EJECT,
        OFF
    }

    private static final int DRIVER_PORT = 0;
    private static final int MANIPULATOR_PORT = 1;

    private static final double DEADBAND = 0.1;
    private static final double TRIGGER_DEADBAND =
            0.2; // Intentionally small to prevent the gamer lock mode from breaking anything

    private static final double DEFAULT_SPEED = 1.5; // Meters per second
    private static final double FAST_SPEED = 4.11; // Meters per second
    private static final CWAngle MAX_ROTATION = CWAngle.rad(Math.PI);

    private static final double NUDGE_PER_PERIODIC = 0.25 * 0.02;

    private static final NTBoolean L_IS_CONE = new NTBoolean("Is Cone", false);

    private final RobotContainer robot;

    private final XboxController driver;
    private final XboxController manipulator;

    private SlewRateLimiter limiter;

    private ArmPosition prevArmTarget;
    private boolean prevWasGrid;

    private GamePiece gamePiece;
    private boolean shouldBeRobotRelative;

    public Input(RobotContainer robot) {
        this.robot = robot;

        L_IS_CONE.setTemporary();

        driver = new XboxController(DRIVER_PORT);
        manipulator = new XboxController(MANIPULATOR_PORT);

        driver.start.onRising(robot.swerveDrive::zeroGyroscope);

        manipulator.leftBumper.onRising(
                () -> {
                    gamePiece = GamePiece.CUBE;
                    robot.messenger.prepare("Robot:GamePiece").addBoolean(false).send();
                    L_IS_CONE.set(false);
                });
        manipulator.rightBumper.onRising(
                () -> {
                    gamePiece = GamePiece.CONE;
                    robot.messenger.prepare("Robot:GamePiece").addBoolean(true).send();
                    L_IS_CONE.set(true);
                });

        prevWasGrid = false;
        prevArmTarget = ArmPositions.DEFAULT.getPosition();
        gamePiece = GamePiece.CUBE;

        /*
         * The limiter acts to reduce sudden acceleration and decelleration when going into or dropping out of
         * fast mode. It doesn't effect the sticks directly as that was not a problem that we faced. Instead,
         * it just effects fast mode ramping.
         */
        SPEED_RATE_LIMIT.nowAndOnChange(
                () -> {
                    double newRate = SPEED_RATE_LIMIT.get();
                    limiter = new SlewRateLimiter(newRate, -newRate, 0);
                });
    }

    /**
     * Pre-process inputs from joysticks
     *
     * @param val Joystick inputs
     * @return Processed outputs
     */
    private double deadband(double val) {
        return MathUtil.deadband(val, DEADBAND);
    }

    // ---- Driver controls ----

    public Vec2d getDriveTranslation() {
        boolean fastMode = driver.rightBumper.isPressed();

        double speed = DEFAULT_SPEED;
        if (fastMode) {
            speed = FAST_SPEED;
        }

        speed = limiter.calculate(speed);

        double x = -deadband(driver.leftStickY.get()) * speed;
        double y = -deadband(driver.leftStickX.get()) * speed;

        return new Vec2d(x, y);
    }

    public Angle getDriveRotation() {
        return MAX_ROTATION.mul(deadband(driver.rightStickX.get()));
    }

    public boolean isRobotRelative() {
        return driver.rightTrigger.get() >= TRIGGER_DEADBAND || shouldBeRobotRelative;
    }

    private void driverPeriodic() {
        boolean driveInput =
                Math.abs(driver.leftStickX.get()) > DEADBAND
                        || Math.abs(driver.leftStickY.get()) > DEADBAND;
        boolean turnInput = Math.abs(driver.rightStickX.get()) > DEADBAND;

//        boolean rumble = (driveInput && snap.snapDrive) || (turnInput || snap.snapTurn);
//        driver.setRumble(rumble ? 0.5 : 0);
    }

    // ---- Manipulator controls ----
    // TODO: New arm controls

    private GamePiece getGamePiece() {
        return gamePiece;
    }

    private boolean isEject() {
        return manipulator.leftTrigger.get() > TRIGGER_DEADBAND;
    }

//    public NTTranslation2d getArmTarget() {
//        if (manipulator.dpad.up.isPressed()) return getArmHigh();
//        if (manipulator.dpad.down.isPressed()) return getArmMid();
//        if (manipulator.b.isPressed()) return getSubstationPickup();
//        if (manipulator.a.isPressed()) {
//            return null; // Home target - position is retrieved from arm subsystem later
//        }
//
//        return ArmPositions.DEFAULT;
//    }
//
//    private NTTranslation2d getArmHigh() {
//        if (getGamePiece() == GamePiece.CUBE) return ArmPositions.CUBE_UPPER;
//        return ArmPositions.CONE_UPPER;
//    }
//
//    private NTTranslation2d getArmMid() {
//        if (getGamePiece() == GamePiece.CUBE) return ArmPositions.CUBE_CENTER;
//        return ArmPositions.CONE_CENTER;
//    }
//
//    private NTTranslation2d getSubstationPickup() {
//        if (getGamePiece() == GamePiece.CUBE) return ArmPositions.CUBE_PICKUP;
//        return ArmPositions.CONE_PICKUP;
//    }

    private IntakeMode getIntakeMode() {
        if (isEject()) return IntakeMode.EJECT;

        if (manipulator.a.isPressed()
                || manipulator.b.isPressed()
                || manipulator.rightTrigger.get() > TRIGGER_DEADBAND) return IntakeMode.INTAKE;

        return IntakeMode.OFF;
    }

    private void manipulatorPeriodic() {
        // if (getGamePiece() == GamePiece.CONE) robot.lights.set(Lights.Color.YELLOW); // TODO: Lights
        // else robot.lights.set(Lights.Color.BLUE);

        IntakeMode intakeMode = getIntakeMode();
        switch (intakeMode) {
            case INTAKE:
                if (manipulator.a.isPressed()) {
                    robot.intake.setExpectedPiece(GamePiece.CUBE);
                } else {
                    robot.intake.setExpectedPiece(getGamePiece());
                }
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
        Translation2d armNudge =
                new Translation2d(
                        deadband(manipulator.rightStickX.get()) * NUDGE_PER_PERIODIC,
                        deadband(-manipulator.rightStickY.get()) * NUDGE_PER_PERIODIC);
        armNudge =
                armNudge.plus(
                        new Translation2d(
                                deadband(manipulator.leftStickX.get()) * NUDGE_PER_PERIODIC,
                                deadband(-manipulator.leftStickY.get()) * NUDGE_PER_PERIODIC));

//        NTTranslation2d ntArmTarget = getArmTarget();
//        boolean isGrid =
//                ntArmTarget != null
//                        && ntArmTarget != ArmPositions.CUBE_PICKUP
//                        && ntArmTarget != ArmPositions.CONE_PICKUP
//                        && ntArmTarget != ArmPositions.DEFAULT;
//        Translation2d armTarget =
//                ntArmTarget == null ? robot.arm.getHomeTarget() : ntArmTarget.getTranslation();
//
//        // If it is moving to a new target
//        if (!armTarget.equals(prevArmTarget) && (isGrid || prevWasGrid)) {
//            armNudge = new Translation2d(0, 0);
//
//            // Move to an intermediate position first
//            robot.arm.setTargetPosition(
//                    new Translation2d(0.6, Math.max(armTarget.getY(), prevArmTarget.getY())));
//            if (!robot.arm.isInTolerance()) {
//                return; // Keep moving to the intermediate position
//            }
//        }
//        prevArmTarget = armTarget;
//        prevWasGrid = isGrid;
//
//        armTarget = armTarget.plus(armNudge);
//        if (ntArmTarget != null && ntArmTarget != ArmPositions.DEFAULT) {
//            ntArmTarget.set(armTarget);
//            prevArmTarget = armTarget;
//        }
//
//        robot.arm.setTargetPosition(armTarget);
    }

    @Override
    public void periodic() {
        if (!DriverStation.isTeleop()) return;
        manipulatorPeriodic();
    }
}
