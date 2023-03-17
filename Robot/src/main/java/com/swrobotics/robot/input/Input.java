package com.swrobotics.robot.input;

import com.swrobotics.lib.input.XboxController;
import com.swrobotics.lib.net.NTTranslation2d;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.swerve.commands.PathfindToPointCommand;
import com.swrobotics.lib.swerve.commands.TurnToAngleCommand;
import com.swrobotics.mathlib.*;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.commands.BalanceSequenceCommand;
import com.swrobotics.robot.positions.ArmPositions;
import com.swrobotics.robot.positions.SnapPositions;
import com.swrobotics.robot.positions.SnapPositions.SnapPosition;
import com.swrobotics.robot.subsystems.Lights;
import com.swrobotics.robot.subsystems.intake.GamePiece;
import com.swrobotics.robot.subsystems.vision.Limelight;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
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
        INTAKE, EJECT, OFF
    }

    private static final int DRIVER_PORT = 0;
    private static final int MANIPULATOR_PORT = 1;

    private static final double DEADBAND = 0.1;
    private static final double TRIGGER_DEADBAND = 0.2; // Intentionally small to prevent the gamer lock mode from breaking anything

    private static final double DEFAULT_SPEED = 1.5; // Meters per second
    private static final double FAST_SPEED = 4.11;   // Meters per second
    private static final Angle MAX_ROTATION = AbsoluteAngle.rad(Math.PI);

    private static final double NUDGE_PER_PERIODIC = 0.25 * 0.02;

    private final RobotContainer robot;
	
    private final XboxController driver;
    private final XboxController manipulator;

    private SlewRateLimiter limiter;

    private final PathfindToPointCommand snapDriveCmd;
    private final TurnToAngleCommand snapTurnCmd;
    private Angle snapAngle;

    private Translation2d prevArmTarget;

    private GamePiece gamePiece;

    public Input(RobotContainer robot) {
        this.robot = robot;

        driver = new XboxController(DRIVER_PORT);
        manipulator = new XboxController(MANIPULATOR_PORT);

        driver.start.onRising(robot.drivetrainSubsystem::zeroGyroscope);

        manipulator.leftBumper.onRising(() -> gamePiece = GamePiece.CUBE);
        manipulator.rightBumper.onRising(() -> gamePiece = GamePiece.CONE);

        snapDriveCmd = new PathfindToPointCommand(robot, null);
        snapTurnCmd = new TurnToAngleCommand(robot, () -> snapAngle, false);

        prevArmTarget = ArmPositions.DEFAULT.getTranslation();
        gamePiece = GamePiece.CUBE;

        /*
         * The limiter acts to reduce sudden acceleration and decelleration when going into or dropping out of
         * fast mode. It doesn't effect the sticks directly as that was not a problem that we faced. Instead,
         * it just effects fast mode ramping.
         */
        double rate = SPEED_RATE_LIMIT.get();
        limiter = new SlewRateLimiter(rate, -rate, 0);
        SPEED_RATE_LIMIT.onChange(() -> {
            double newRate = SPEED_RATE_LIMIT.get();
            limiter = new SlewRateLimiter(newRate, -newRate, 0);
        });
    }

    /**
     * Pre-process inputs from joysticks
     * @param val Joystick inputs
     * @return Processed outputs
     */
    private double deadband(double val) {
        double deadbanded = MathUtil.deadband(val, DEADBAND);
        double mapped = MathUtil.map(Math.abs(deadbanded), 0.0, 1.0, DEADBAND, 1.0);
        return Math.copySign(mapped, deadbanded);
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
        return MAX_ROTATION.cw().mul(deadband(driver.rightStickX.get()));
    }

    public boolean isRobotRelative() {
        return driver.rightTrigger.get() >= TRIGGER_DEADBAND;
    }

    private void disableSnap() {
        setCommandEnabled(snapDriveCmd, false);
        setCommandEnabled(snapTurnCmd, false);
        driver.setRumble(0);
    }

    private void driverPeriodic() {
        if (!driver.leftBumper.isPressed()) {
            disableSnap();
            return;
        }

        // Calculate snapping
        Pose2d currentPose = robot.drivetrainSubsystem.getPose();
        SnapPositions.SnapStatus snap = SnapPositions.getSnap(currentPose);
        if (snap == null) {
            disableSnap();
            return;
        }

        if (snap.snapDrive) {
            snapToPosition(snap.pose.getTranslation());
        } else {
            setCommandEnabled(snapDriveCmd, false);
        }

        if (snap.snapTurn) {
            Rotation2d poseAngle = snap.pose.getRotation();

            switch (snap.turnMode) {
                case DIRECT_TURN:
                    snapAngle = CCWAngle.rad(poseAngle.getRadians());
                    break;
                case CONE_NODE_AIM:
                    robot.limelight.setPipeline(Limelight.CONE_NODE_PIPELINE);
                    snapAngle = CCWAngle.rad(currentPose.getRotation().getRadians() - robot.limelight.getXAngle().getRadians());
                    break;
                case GAME_PIECE_AIM:
                    robot.limelight.setPipeline(getGamePiece() == GamePiece.CONE ? Limelight.CONE_PIPELINE : Limelight.CUBE_PIPELINE);
                    snapAngle = CCWAngle.rad(currentPose.getRotation().getRadians() - robot.limelight.getXAngle().getRadians());
                    break;
            }

            snapToAngle(poseAngle);
        } else {
            setCommandEnabled(snapTurnCmd, false);
        }

        boolean driveInput = Math.abs(driver.leftStickX.get()) > DEADBAND || Math.abs(driver.leftStickY.get()) > DEADBAND;
        boolean turnInput = Math.abs(driver.rightStickX.get()) > DEADBAND;

        boolean rumble = (driveInput && snap.snapDrive) || (turnInput || snap.snapTurn);
        driver.setRumble(rumble ? 0.5 : 0);
    }

    // ---- Manipulator controls ----

    private GamePiece getGamePiece() {
        return gamePiece;
    }

    private boolean isEject() {
        return manipulator.leftTrigger.get() > 0.8;
    }
	

    public NTTranslation2d getArmTarget() {
        if (manipulator.dpad.up.isPressed())
			return getArmHigh();
        if (manipulator.dpad.down.isPressed())
			return getArmMid();
        if (manipulator.b.isPressed())
			return getSubstationPickup();
        if (manipulator.a.isPressed())
            return null; // Home target - position is retrieved from arm subsystem later

        return ArmPositions.DEFAULT;
    }

    private NTTranslation2d getArmHigh() {
        if (getGamePiece() == GamePiece.CUBE)
            return ArmPositions.CUBE_UPPER;
        return ArmPositions.CONE_UPPER;
    }

    private NTTranslation2d getArmMid() {
        if (getGamePiece() == GamePiece.CUBE)
            return ArmPositions.CUBE_CENTER;
        return ArmPositions.CONE_CENTER;
    }

    private NTTranslation2d getSubstationPickup() {
        if (getGamePiece() == GamePiece.CUBE)
            return ArmPositions.CUBE_PICKUP;
        return ArmPositions.CONE_PICKUP;
    }

    private IntakeMode getIntakeMode() {
        if (isEject())
            return IntakeMode.EJECT;

        if (manipulator.a.isPressed() || manipulator.b.isPressed() || manipulator.rightTrigger.get() > 0.8)
            return IntakeMode.INTAKE;

        return IntakeMode.OFF;
    }

    private void setCommandEnabled(Command cmd, boolean enabled) {
        if (enabled && !cmd.isScheduled())
            cmd.schedule();
        if (!enabled && cmd.isScheduled())
            cmd.cancel();
    }

    private static final double SNAP_DRIVE_TOL = 0.05;
    private static final double SNAP_TURN_TOL = Math.toRadians(2);

    private void snapToPosition(Translation2d position) {
        snapDriveCmd.setGoal(new Vec2d(position.getX(), position.getY()));

        Pose2d currentPose = robot.drivetrainSubsystem.getPose();
        double dist = currentPose.getTranslation().minus(position).getNorm();

        setCommandEnabled(snapDriveCmd, dist > SNAP_DRIVE_TOL);
    }

    private void snapToAngle(Rotation2d angle) {
        Pose2d currentPose = robot.drivetrainSubsystem.getPose();

        double angleDiff = CCWAngle.rad(angle.getRadians())
            .getAbsDiff(CCWAngle.rad(currentPose.getRotation().getRadians())).rad();

        setCommandEnabled(snapTurnCmd, angleDiff > SNAP_TURN_TOL);
    }

    private void manipulatorPeriodic() {
        if (getGamePiece() == GamePiece.CONE)
            robot.lights.set(Lights.Color.YELLOW);
        else
            robot.lights.set(Lights.Color.BLUE);

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
        Translation2d armNudge = new Translation2d(
            deadband(manipulator.rightStickX.get()) * NUDGE_PER_PERIODIC,
            deadband(-manipulator.rightStickY.get()) * NUDGE_PER_PERIODIC
        );
        armNudge = armNudge.plus(new Translation2d(
                deadband(manipulator.leftStickX.get()) * NUDGE_PER_PERIODIC,
                deadband(-manipulator.leftStickY.get()) * NUDGE_PER_PERIODIC
        ));

        NTTranslation2d ntArmTarget = getArmTarget();
        Translation2d armTarget = ntArmTarget == null ? robot.arm.getHomeTarget() : ntArmTarget.getTranslation();

        // If it is moving to a new target
        if (!armTarget.equals(prevArmTarget)) {
            armNudge = new Translation2d(0, 0);

            // Move to an intermediate position first
            robot.arm.setTargetPosition(new Translation2d(0.6, Math.max(armTarget.getY(), prevArmTarget.getY())));
            if (!robot.arm.isInTolerance()) {
                return; // Keep moving to the intermediate position
            }
        }
        prevArmTarget = armTarget;

        armTarget = armTarget.plus(armNudge);
        if (ntArmTarget != null && ntArmTarget != ArmPositions.DEFAULT) {
            ntArmTarget.set(armTarget);
            prevArmTarget = armTarget;
        }

        robot.arm.setTargetPosition(armTarget);
    }

    @Override
    public void periodic() {
        if (!DriverStation.isTeleop())
            return;

        driverPeriodic();
        manipulatorPeriodic();
    }
}
