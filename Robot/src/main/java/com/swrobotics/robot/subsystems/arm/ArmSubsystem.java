package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.lib.net.*;
import com.swrobotics.lib.schedule.SwitchableSubsystemBase;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.robot.CANAllocation;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;

import java.util.List;

import static com.swrobotics.robot.subsystems.arm.ArmConstants.*;

public final class ArmSubsystem extends SwitchableSubsystemBase {
    private static final NTDouble MOVE_KP = new NTDouble("Arm/Move PID/kP", 8);
    private static final NTDouble MOVE_KI = new NTDouble("Arm/Move PID/kI", 0);
    private static final NTDouble MOVE_KD = new NTDouble("Arm/Move PID/kD", 0);
    public static final NTDouble WRIST_KP = new NTDouble("Arm/Wrist PID/kP", 0.1);
    public static final NTDouble WRIST_KI = new NTDouble("Arm/Wrist PID/kI", 0);
    public static final NTDouble WRIST_KD = new NTDouble("Arm/Wrist PID/kD", 0);

    private static final NTDouble MAX_SPEED = new NTDouble("Arm/Max Speed", 1.0);
    private static final NTDouble STOP_TOL = new NTDouble("Arm/Stop Tolerance", 1.5);
    private static final NTDouble START_TOL = new NTDouble("Arm/Start Tolerance", 2.5);

    private static final NTBoolean CALIBRATE_CANCODERS = new NTBoolean("Arm/Offsets/Calibrate", false);
    private static final NTAngle BOTTOM_OFFSET = new NTAngle("Arm/Offsets/Bottom", Angle.ZERO, NTAngle.Mode.CCW_DEG);
    private static final NTAngle TOP_OFFSET = new NTAngle("Arm/Offsets/Top", Angle.ZERO, NTAngle.Mode.CCW_DEG);
    private static final NTAngle WRIST_OFFSET = new NTAngle("Arm/Offsets/Wrist", Angle.ZERO, NTAngle.Mode.CCW_DEG);

    // FIXME: These defaults are completely arbitrary
    private static final NTVec2d FOLD_ZONE = new NTVec2d("Arm/Fold Zone", 0.5, 0.25);
    private static final NTAngle FOLD_ANGLE = new NTAngle("Arm/Fold Angle", Angle.ZERO, NTAngle.Mode.CCW_DEG);

    private final ArmJoint bottom, top;
    private final WristJoint wrist;
    private final ArmPathfinder pathfinder;
    private final PIDController movePid;
    private ArmPose targetPose;
    private boolean inToleranceHysteresis;

    private final ArmVisualizer currentVisualizer, stepTargetVisualizer, targetVisualizer;

    public ArmSubsystem(MessengerClient msg) {
        bottom = new ArmJoint(CANAllocation.ARM_BOTTOM_MOTOR, CANAllocation.ARM_BOTTOM_CANCODER, CANCODER_TO_ARM_RATIO, BOTTOM_GEAR_RATIO, BOTTOM_OFFSET, true);
        top = new ArmJoint(CANAllocation.ARM_TOP_MOTOR, CANAllocation.ARM_TOP_CANCODER, CANCODER_TO_ARM_RATIO, TOP_GEAR_RATIO, TOP_OFFSET, false);
        wrist = new WristJoint(CANAllocation.ARM_WRIST_MOTOR, CANAllocation.ARM_WRIST_CANCODER, WRIST_CANCODER_TO_ARM_RATIO, WRIST_GEAR_RATIO, WRIST_OFFSET, false); // Invert may be wrong

        double size = (BOTTOM_LENGTH + TOP_LENGTH + WRIST_RAD) * 2;
        Mechanism2d visualizer = new Mechanism2d(size, size);
        targetVisualizer = new ArmVisualizer(size/2, size/2, visualizer, "Target", Color.kDarkRed, Color.kRed, Color.kOrangeRed);
        stepTargetVisualizer = new ArmVisualizer(size/2, size/2, visualizer, "Step Target", Color.kDarkOrange, Color.kOrange, Color.kDarkGoldenrod);
        currentVisualizer = new ArmVisualizer(size/2, size/2, visualizer, "Current", Color.kDarkGreen, Color.kGreen, Color.kLightGreen);
        SmartDashboard.putData("Arm Visualizer", visualizer);

        ArmPose home = ArmPositions.DEFAULT.getPosition().toPose();
        System.out.println("Home pose: " + home);
        if (home == null)
            throw new IllegalStateException("Home position must be valid!");
        bottom.calibratePosition(home.bottomAngle);
        top.calibratePosition(home.topAngle.ccw().wrapDeg(-270, 90));
        wrist.calibratePosition(home.wristAngle);

        pathfinder = new ArmPathfinder(msg);
        movePid = NTUtil.tunablePID(MOVE_KP, MOVE_KI, MOVE_KD);
        targetPose = home;
    }

    public ArmPose getCurrentPose() {
        Angle bottomAngle = bottom.getCurrentAngle();
        Angle topAngle = top.getCurrentAngle();
        Angle wristAngle = wrist.getCurrentAngle().add(topAngle); // Convert from relative to top segment to relative to horizontal
        return new ArmPose(bottomAngle, topAngle, wristAngle);
    }

    public ArmPose getTargetPose() {
        return targetPose;
    }

    // Converts each axis to motor rotation count
    // This biases path following towards the route where each axis takes equal time
    private Vec2d bias(ArmPathfinder.PathPoint point) {
        return new Vec2d(
                point.bottomAngle.ccw().rot() * ArmConstants.BOTTOM_GEAR_RATIO,
                point.topAngle.ccw().rot() * ArmConstants.TOP_GEAR_RATIO);
    }

//    int counter = 0;
    @Override
    public void periodic() {
        boolean brake = !DriverStation.isDisabled();
        bottom.setBrakeMode(brake);
        top.setBrakeMode(brake);
        wrist.setBrakeMode(brake);

//        if (counter++ == 100) {
//            counter = 0;
//
//            ArmPose pose = null;
//            while (pose == null) {
//                pose = new ArmPosition(new Vec2d(
//                        Math.random() * 2 - 1,
//                        Math.random() + 0.2
//                ), CCWAngle.deg(Math.random() * 180 - 90)).toPose();
//            }
//
//            setTargetPose(pose);
//        }

        if (CALIBRATE_CANCODERS.get()) {
            CALIBRATE_CANCODERS.set(false);

            // Assume arm is already at home position
            bottom.calibrateCanCoder();
            top.calibrateCanCoder();
            wrist.calibrateCanCoder();
        }

        currentVisualizer.setPose(getCurrentPose());
        targetVisualizer.setPose(targetPose);

        // Send the desired path endpoints to the pathfinder
        ArmPose currentPose = getCurrentPose();
        ArmPathfinder.PathPoint startPoint = ArmPathfinder.PathPoint.fromPose(currentPose);
        ArmPathfinder.PathPoint targetPoint = ArmPathfinder.PathPoint.fromPose(targetPose);
        pathfinder.setEndpoints(startPoint, targetPoint);

        Vec2d biasedStart = bias(startPoint);
        Vec2d biasedTarget = bias(targetPoint);

        List<ArmPathfinder.PathPoint> path = pathfinder.getPath();
        ArmPose currentTarget = null;
        if (path == null || !pathfinder.isPathValid()) {
            // Pathfinder either is not connected or hasn't found a path yet, so
            // assume a straight line in state space is valid. This is true in
            // most cases
            currentTarget = targetPose;
        } else {
            // Find which segment of the path we are currently closest to
            double minDist = Double.POSITIVE_INFINITY;
            for (int i = path.size() - 1; i > 0; i--) {
                ArmPathfinder.PathPoint pose = path.get(i);
                Vec2d point = bias(pose);
                Vec2d prev = bias(path.get(i - 1));

                double dist = biasedStart.distanceToLineSegmentSq(point, prev);

                if (dist < minDist) {
                    // Target the segment's endpoint
                    currentTarget = new ArmPose(pose.bottomAngle, pose.topAngle, targetPose.wristAngle);
                    minDist = dist;
                }
            }

            // This should never happen, since the path should always have at least two points (start, goal)
            if (currentTarget == null) {
                onDisable();
                return;
            }
        }
        stepTargetVisualizer.setPose(currentTarget);

        // Find a vector to the current intermediate step in non-biased state space
        double topAngle = MathUtil.wrap(currentTarget.topAngle.ccw().rad(), -1.5 * Math.PI, 0.5 * Math.PI);
        Vec2d towardsTarget = new Vec2d(currentTarget.bottomAngle.ccw().rad(), topAngle)
                .sub(currentPose.bottomAngle.ccw().rad(), currentPose.topAngle.ccw().rad());

        // Tolerance hysteresis so the motor doesn't do the shaky shaky
        double magSqToFinalTarget = new Vec2d(biasedTarget).sub(biasedStart).magnitudeSq();
        boolean prevInTolerance = inToleranceHysteresis;

        // If within stop tolerance, stop moving
        // If outside start tolerance, start moving
        // Otherwise, continue doing what we were doing the previous periodic
        double startTol = START_TOL.get();
        double stopTol = STOP_TOL.get();
        if (magSqToFinalTarget > startTol * startTol) {
            inToleranceHysteresis = false;
        } else if (magSqToFinalTarget < stopTol * stopTol) {
            inToleranceHysteresis = true;
        }

        // If we just started moving, we need to reset the PID in case there
        // is any nonzero value in the integral accumulator
        if (prevInTolerance && !inToleranceHysteresis)
            movePid.reset();

        if (inToleranceHysteresis) {
            // Already at target, we don't need to move
            onDisable();
        } else {
            // Negated since PID is calculating from the current distance
            // towards 0, so negative PID output corresponds to movement
            // towards the target.
            // Magnitude to final target is used so movement only slows down
            // upon reaching the final target, not at each intermediate position
            double pidOut = -movePid.calculate(Math.sqrt(magSqToFinalTarget), 0);
            pidOut = MathUtil.clamp(pidOut, 0, MAX_SPEED.get());

            // Apply bias to towardsTarget so that each axis takes equal time
            // This allows the assumption in the pathfinder that moving towards
            // a target travels in a straight line in state space
            towardsTarget.mul(ArmConstants.BOTTOM_GEAR_RATIO, ArmConstants.TOP_GEAR_RATIO)
                    .boxNormalize().mul(pidOut);

            // Set motor outputs to move towards the current target
            bottom.setMotorOutput(towardsTarget.x);
            top.setMotorOutput(towardsTarget.y);
        }

        // Always fold regardless of having a game piece, since we can't
        // reliably determine if we have one. This is fine without game
        // piece since the intake fits over the drive base at all angles.
        Vec2d axisPos = currentPose.getAxisPos().absolute();
        Vec2d foldZone = FOLD_ZONE.getVec();
        Angle wristRef = currentPose.topAngle;
        if (axisPos.x <= foldZone.x && axisPos.y <= foldZone.y) {
            // Set wrist to fold angle
            wrist.setTargetAngle(FOLD_ANGLE.get().sub(wristRef));
        } else {
            // Set wrist to final target angle
            wrist.setTargetAngle(targetPose.wristAngle.sub(wristRef));
        }
    }

    // TODO: Don't have this, this allows the possibility of targeting invalid/unsafe positions
    private void setTargetPose(ArmPose targetPose) {
        this.targetPose = targetPose;
    }

    public void setTargetPosition(ArmPosition targetPosition) {
        ArmPose pose = targetPosition.toPose();
        if (pose == null) {
            System.err.println("Trying to set arm to invalid position");
            return;
        }
        targetPose = pose;
    }

    public void moveNow() {
        inToleranceHysteresis = false;
    }

    public boolean isInTolerance() {
        return inToleranceHysteresis;
    }

    @Override
    protected void onDisable() {
        bottom.setMotorOutput(0);
        top.setMotorOutput(0);
        wrist.setMotorOutput(0);
    }
}
