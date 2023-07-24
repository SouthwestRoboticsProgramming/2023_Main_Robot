package com.swrobotics.robot.input;

import com.swrobotics.lib.drive.swerve.SwerveModuleAttributes;
import com.swrobotics.lib.drive.swerve.StopPosition;
import com.swrobotics.lib.input.XboxController;
import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.time.Duration;
import com.swrobotics.lib.time.TimeUnit;
import com.swrobotics.lib.util.ValueDebouncer;
import com.swrobotics.mathlib.*;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.config.NTData;
import com.swrobotics.robot.subsystems.arm.ArmConstants;
import com.swrobotics.robot.subsystems.arm.ArmPosition;
import com.swrobotics.robot.subsystems.arm.ArmPositions;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.intake.GamePiece;

import com.swrobotics.robot.subsystems.intake.IntakeSubsystem;
import edu.wpi.first.math.filter.SlewRateLimiter;
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
     *  Start: calibrate current target to current pose
     *
     * Driver:
     *  left stick:    drive translation
     *  right stick:   drive rotation
     *  right bumper:  fast mode
     *  L+R bumpers:   really fast mode
     *  start:         reset gyro
     *
     */

    private static final int DRIVER_PORT = 0;
    private static final int MANIPULATOR_PORT = 1;

    private static final double DEADBAND = 0.1;
    private static final double TRIGGER_DEADBAND =
            0.2; // Intentionally small to prevent the gamer lock mode from breaking anything

    private static final double DEFAULT_SPEED = 1.5; // Meters per second
    private static final double FAST_SPEED = 4.11; // Meters per second
    private static final double REALLY_FAST_SPEED = SwerveModuleAttributes.SDS_MK4I_L3.getMaxVelocity();
    private static final CWAngle MAX_ROTATION = CWAngle.rad(Math.PI);

    private static final NTBoolean L_IS_CONE = new NTBoolean("Is Cone", false);

    private final RobotContainer robot;

    private final XboxController driver;
    private SlewRateLimiter limiter;

    private final XboxController manipulator;
    private GamePiece gamePiece;
    private final Vec2d defaultArmNudgePosition;
    private Angle defaultArmNudgeAngle;
    private final ValueDebouncer<ArmPosition.NT> armPositionDebounce;
    private ArmPosition.NT prevArmTarget;

    public Input(RobotContainer robot) {
        this.robot = robot;

        driver = new XboxController(DRIVER_PORT, DEADBAND);
        manipulator = new XboxController(MANIPULATOR_PORT, DEADBAND);

        driver.start.onRising(robot.swerveDrive::zeroGyroscope);
        driver.back.onRising(robot.swerveDrive::zeroGyroscopeBackwards);

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
        armPositionDebounce = new ValueDebouncer<>(new Duration(0.05, TimeUnit.SECONDS), ArmPositions.DEFAULT);
        prevArmTarget = ArmPositions.DEFAULT;

        /*
         * The limiter acts to reduce sudden acceleration and deceleration when going into or dropping out of
         * fast mode. It doesn't effect the sticks directly as that was not a problem that we faced. Instead,
         * it just effects fast mode ramping.
         */
        NTData.INPUT_SPEED_RATE_LIMIT.nowAndOnChange(
                (newRate) -> {
                    limiter = new SlewRateLimiter(newRate, -newRate, 0);
                });
    }

    // ---- Driver controls ----

    public Vec2d getDriveTranslation() {
        double speed = DEFAULT_SPEED;
        if (driver.rightBumper.isPressed()) {
            speed = driver.leftBumper.isPressed() ? REALLY_FAST_SPEED : FAST_SPEED;
        }

        speed = limiter.calculate(speed);

        double x = -driver.leftStickY.get() * speed;
        double y = -driver.leftStickX.get() * speed;

        return new Vec2d(x, y);
    }

    public Angle getDriveRotation() {
        return MAX_ROTATION.mul(driver.rightStickX.get());
    }

    public boolean isRobotRelative() {
        return driver.rightTrigger.get() >= TRIGGER_DEADBAND;
    }

    private void driverPeriodic() {
        boolean coast = driver.leftTrigger.get() >= TRIGGER_DEADBAND;
        robot.swerveDrive.setStopPosition(coast ? StopPosition.COAST : StopPosition.FORWARD);
    }

    // ---- Manipulator controls ----

    private ArmPosition.NT inferDirection(ArmPositions.FrontBackPair pair, Angle currentAngle, Angle relativeForward) {
        Vec2d currentAngleVec = new Vec2d(currentAngle, 1);
        Vec2d relativeForwardVec = new Vec2d(relativeForward, 1);

        boolean isFront = currentAngleVec.dot(relativeForwardVec) >= 0;
        if (manipulator.back.isPressed())
            isFront = !isFront;

        return isFront ? pair.front : pair.back;
//        return manipulator.back.isPressed() ? pair.back : pair.front;
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

    // TODO: Cones in back only
    private void manipulatorPeriodic() {
        if (DriverStation.isAutonomous())
            return;

        Angle angle = Angle.fromRotation2d(robot.swerveDrive.getPose().getRotation());

        IntakeSubsystem.Mode intakeMode = IntakeSubsystem.Mode.OFF;
        GamePiece effectiveGamePiece = gamePiece;
        if (manipulator.leftTrigger.get() > TRIGGER_DEADBAND)
            intakeMode = IntakeSubsystem.Mode.INTAKE;
        if (manipulator.rightTrigger.get() > TRIGGER_DEADBAND)
            intakeMode = IntakeSubsystem.Mode.EJECT;

        ArmPosition.NT ntArmTarget = ArmPositions.DEFAULT;
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
            // TEMP FIX: Disable cone pickup in the back
//            if (gamePiece == GamePiece.CUBE) {
                if (manipulator.y.isPressed()) {
//                    ntArmTarget = ArmPositions.DOWNED_CONE_FLOOR_PICKUP.back;
//                    effectiveGamePiece = GamePiece.CONE;
                } else {
                    ntArmTarget = gamePieceSet.floorPickup.back;
                }
//            }
            intakeMode = IntakeSubsystem.Mode.INTAKE;
        } else if (manipulator.y.isPressed()) {
            ntArmTarget = inferDirection(gamePieceSet.substationPickup, angle, towardsSubstationAngle());
            intakeMode = IntakeSubsystem.Mode.INTAKE;
        }

        if (manipulator.dpad.up.isPressed()) {
            // We can only do high in one direction
            ntArmTarget = gamePieceSet.scoreHigh;
        }
        if (manipulator.dpad.down.isPressed()) {
            ntArmTarget = inferDirection(gamePieceSet.scoreMid, angle, towardsGridAngle());
        }

        Vec2d translationNudge = manipulator.getLeftStick().mul(NTData.INPUT_ARM_TRANSLATION_RATE.get()).mul(1, -1);
        Angle wristNudge = NTData.INPUT_WRIST_ROTATION_RATE.get().mul(manipulator.rightStickY.get());

        // No shakey
        if (translationNudge.magnitudeSq() > 0)
            robot.arm.moveNow();

        boolean masonNeedsToTurnTheRobot = ntArmTarget == null;
//        driver.setRumble(masonNeedsToTurnTheRobot ? 0.5 : 0);
        if (masonNeedsToTurnTheRobot)
            ntArmTarget = ArmPositions.DEFAULT;

//        ntArmTarget = armPositionDebounce.debounce(ntArmTarget);

        ArmPosition armTarget;
        if (ntArmTarget == ArmPositions.DEFAULT) {
//            defaultArmNudgePosition.add(translationNudge);
//            defaultArmNudgeAngle = defaultArmNudgeAngle.add(wristNudge);

            ArmPosition def = ntArmTarget.get();
            armTarget = new ArmPosition(def.axisPos.add(defaultArmNudgePosition), def.wristAngle.add(defaultArmNudgeAngle));
        } else {
            defaultArmNudgePosition.set(0, 0);
            defaultArmNudgeAngle = Angle.ZERO;

            ArmPosition raw = ntArmTarget.get();
            ArmPosition newArmTarget = new ArmPosition(raw.axisPos.add(translationNudge), raw.wristAngle.add(wristNudge));
            if (newArmTarget.toPose() != null) {
                armTarget = newArmTarget;
            } else if (manipulator.start.isRising()) {
                armTarget = robot.arm.getCurrentPose().toPosition();
            } else {
                armTarget = raw;
            }

            ntArmTarget.set(armTarget);
        }

        // Bring back pose if too far
        if (armTarget.axisPos.magnitudeSq() > MathUtil.square(ArmConstants.BOTTOM_LENGTH + ArmConstants.TOP_LENGTH)) {
            armTarget.axisPos.normalize().mul(ArmConstants.BOTTOM_LENGTH + ArmConstants.TOP_LENGTH - 0.2);
        }

        robot.intake.set(intakeMode, effectiveGamePiece);

        // If changing target and either current or new target requires
        // intermediate position, go so intermediate position first
        if (NTData.ARM_INTERMEDIATE_ENABLE.get() && prevArmTarget != ntArmTarget && (prevArmTarget.useIntermediate() || ntArmTarget.useIntermediate())) {
            // Choose intermediate point between points, at highest level
            ArmPosition intermediatePos = prevArmTarget.get();
            double pct = NTData.ARM_INTERMEDIATE_PERCENT.get();
            if (armTarget.axisPos.y < intermediatePos.axisPos.y)
                pct = 1 - pct;

            intermediatePos.axisPos.x = MathUtil.lerp(intermediatePos.axisPos.x, armTarget.axisPos.x, pct);
            intermediatePos.axisPos.y = Math.max(intermediatePos.axisPos.y, armTarget.axisPos.y);
            robot.arm.setTargetPosition(intermediatePos);

            // If reached intermediate position, move on to actual target
            if (robot.arm.isInToleranceImmediate()) {
                prevArmTarget = ntArmTarget;
                robot.arm.setTargetPosition(armTarget);
            }
        } else {
            robot.arm.setTargetPosition(armTarget);
        }
    }

    @Override
    public void periodic() {
//        if (!DriverStation.isTeleop()) return;
        driverPeriodic();
        manipulatorPeriodic();
    }
}
