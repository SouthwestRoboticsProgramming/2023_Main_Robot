package com.swrobotics.robot.input;

import com.swrobotics.lib.input.XboxController;
import com.swrobotics.lib.net.NTAngle;
import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.mathlib.*;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.arm.ArmPosition;
import com.swrobotics.robot.subsystems.arm.ArmPositions;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.intake.GamePiece;

import com.swrobotics.robot.subsystems.intake.IntakeSubsystem;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public final class Input extends SubsystemBase {
    /*
     * Manipulator:
     *.  Left bumper: set to cube mode
     *.  Right bumper: set to cone mode
     *  Left trigger: intake
     *  Right trigger: eject
     *.  Dpad up: score high
     *.  Dpad down: score mid
     *.  A: floor pickup front
     *.  B: floor pickup back
     *.  X: chute pickup
     *.  Y: double substation pickup
     *.  A+X: floor pickup downed cone front
     *.  B+Y: floor pickup downed cone back
     *.  Default: default
     *  Left stick: arm	joint tune/manual control
     *  Right stick Y: wrist tune/manual control
     *    Tune modifies	stored position, except	default	doesn't	change
     *
     * Driver:
     *  left stick:    drive translation
     *  right stick:   drive rotation
     *  right bumper:  fast mode
     *  start:         reset gyro
     *
     */

    private static final NTDouble SPEED_RATE_LIMIT = new NTDouble("Input/Speed Slew Limit", 20);
    private static final NTDouble ARM_TRANSLATION_RATE = new NTDouble("Input/Arm/Nudge Translation Rate", 1);
    private static final NTAngle WRIST_ROTATION_RATE = new NTAngle("Input/Arm/Nudge Wrist Rate", CCWAngle.deg(90), NTAngle.Mode.CCW_DEG);

    private static final int DRIVER_PORT = 0;
    private static final int MANIPULATOR_PORT = 1;

    private static final double DEADBAND = 0.1;
    private static final double TRIGGER_DEADBAND =
            0.2; // Intentionally small to prevent the gamer lock mode from breaking anything

    private static final double DEFAULT_SPEED = 1.5; // Meters per second
    private static final double FAST_SPEED = 4.11; // Meters per second
    private static final CWAngle MAX_ROTATION = CWAngle.rad(Math.PI);

    private static final NTBoolean L_IS_CONE = new NTBoolean("Is Cone", false);

    private final RobotContainer robot;

    private final XboxController driver;
    private SlewRateLimiter limiter;

    private final XboxController manipulator;
    private GamePiece gamePiece;
    private Vec2d defaultArmNudgePosition;
    private Angle defaultArmNudgeAngle;

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

        gamePiece = GamePiece.CUBE;
        defaultArmNudgePosition = new Vec2d(0, 0);
        defaultArmNudgeAngle = Angle.ZERO;

        /*
         * The limiter acts to reduce sudden acceleration and deceleration when going into or dropping out of
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
        return driver.rightTrigger.get() >= TRIGGER_DEADBAND;
    }

    // ---- Manipulator controls ----

    private ArmPosition.NT inferDirection(ArmPositions.FrontBackPair pair, Angle currentAngle, Angle relativeForward) {
        Vec2d currentAngleVec = new Vec2d(currentAngle, 1);
        Vec2d relativeForwardVec = new Vec2d(relativeForward, 1);

        return currentAngleVec.dot(relativeForwardVec) >= 0 ? pair.front : pair.back;
    }

    private Angle towardsChuteAngle() {
        return CCWAngle.deg(90); // Always on the top wall regardless of alliance
    }

    private Angle towardsSubstationAngle() {
        return DrivetrainSubsystem.FIELD.getAllianceForwardAngle();
    }

    private Angle towardsGridAngle() {
        return DrivetrainSubsystem.FIELD.getAllianceReverseAngle();
    }

    private void manipulatorPeriodic() {
        Angle angle = Angle.fromRotation2d(robot.swerveDrive.getPose().getRotation());

        IntakeSubsystem.Mode intakeMode = IntakeSubsystem.Mode.OFF;
        GamePiece effectiveGamePiece = gamePiece;
        if (manipulator.leftTrigger.get() > TRIGGER_DEADBAND)
            intakeMode = IntakeSubsystem.Mode.INTAKE;
        if (manipulator.rightTrigger.get() > TRIGGER_DEADBAND)
            intakeMode = IntakeSubsystem.Mode.EJECT;

        ArmPosition.NT ntArmTarget = null;
        ArmPositions.PositionSet gamePieceSet = gamePiece == GamePiece.CONE ? ArmPositions.CONE : ArmPositions.CUBE;

        if (manipulator.a.isPressed()) {
            if (manipulator.x.isPressed()) {
                ntArmTarget = ArmPositions.DOWNED_CONE_FLOOR_PICKUP.front;
                effectiveGamePiece = GamePiece.CONE;
            } else {
                ntArmTarget = gamePieceSet.floorPickup.front;
            }
            intakeMode = IntakeSubsystem.Mode.INTAKE;
        } else if (manipulator.x.isPressed()) {
            ntArmTarget = inferDirection(gamePieceSet.chutePickup, angle, towardsChuteAngle());
            intakeMode = IntakeSubsystem.Mode.INTAKE;
        }

        if (manipulator.b.isPressed()) {
            if (manipulator.y.isPressed()) {
                ntArmTarget = ArmPositions.DOWNED_CONE_FLOOR_PICKUP.back;
                effectiveGamePiece = GamePiece.CONE;
            } else {
                ntArmTarget = gamePieceSet.floorPickup.back;
            }
            intakeMode = IntakeSubsystem.Mode.INTAKE;
        } else if (manipulator.y.isPressed()) {
            ntArmTarget = inferDirection(gamePieceSet.substationPickup, angle, towardsSubstationAngle());
            intakeMode = IntakeSubsystem.Mode.INTAKE;
        }

        if (manipulator.dpad.up.isPressed()) {
            // We can only do high on front
            ntArmTarget = gamePieceSet.scoreHighFront;
        }
        if (manipulator.dpad.down.isPressed()) {
            ntArmTarget = inferDirection(gamePieceSet.scoreMid, angle, towardsGridAngle());
        }

        Vec2d translationNudge = deadbandVec(manipulator.getLeftStick()).mul(ARM_TRANSLATION_RATE.get()).mul(1, -1);
        Angle wristNudge = WRIST_ROTATION_RATE.get().mul(deadband(manipulator.rightStickY.get()));

        // No shakey
        if (translationNudge.magnitudeSq() > 0)
            robot.arm.moveNow();

        ArmPosition armTarget;
        if (ntArmTarget == null) {
            defaultArmNudgePosition.add(translationNudge);
            defaultArmNudgeAngle = defaultArmNudgeAngle.add(wristNudge);

            ntArmTarget = ArmPositions.DEFAULT;
            ArmPosition def = ntArmTarget.getPosition();
            armTarget = new ArmPosition(def.axisPos.add(defaultArmNudgePosition), def.wristAngle.add(defaultArmNudgeAngle));
        } else {
            defaultArmNudgePosition.set(0, 0);
            defaultArmNudgeAngle = Angle.ZERO;

            ArmPosition raw = ntArmTarget.getPosition();
            armTarget = new ArmPosition(raw.axisPos.add(translationNudge), raw.wristAngle.add(wristNudge));
            ntArmTarget.set(armTarget);
        }

        robot.arm.setTargetPosition(armTarget);
        robot.intake.set(intakeMode, effectiveGamePiece);
    }

    private Vec2d deadbandVec(Vec2d v) {
        double mag = deadband(v.magnitude());
        return v.normalize().mul(mag);
    }

    @Override
    public void periodic() {
        if (!DriverStation.isTeleop()) return;
        manipulatorPeriodic();
    }
}
