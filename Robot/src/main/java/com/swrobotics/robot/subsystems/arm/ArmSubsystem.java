package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.lib.net.*;
import com.swrobotics.lib.schedule.SwitchableSubsystemBase;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.robot.config.CANAllocation;
import com.swrobotics.robot.subsystems.intake.GamePiece;
import com.swrobotics.robot.subsystems.intake.IntakeSubsystem;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import org.littletonrobotics.junction.Logger;

import java.util.List;

import static com.swrobotics.robot.subsystems.arm.ArmConstants.*;
import static com.swrobotics.robot.config.NTData.*;

// CanCoder calibration procedure:
// 1. Disable the robot
// 2. Set Arm/Brake Mode to false
// 3. Move the arm such that the top and bottom joints are vertical
// 4. Hold the intake such that the wrist motor's shaft is directly below the wrist axis
// 5. Set Arm/Offsets/Calibrate to true
// 6. Set Arm/Brake Mode to true
// 6. Restart robot code to use the new offsets
public final class ArmSubsystem extends SwitchableSubsystemBase {
    private static final NTPrimitive<Boolean> CALIBRATE_CANCODERS = new NTBoolean("Arm/Offsets/Calibrate", false);

    private final IntakeSubsystem intake;
    private final ArmJoint bottom, top;
    private final WristJoint wrist;
    private final ArmPathfinder pathfinder;
    private final PIDController movePid;
    private ArmPose targetPose;
    private boolean inToleranceHysteresis;
    private NTEntry<Angle> wristFold;

    private final ArmVisualizer currentVisualizer, stepTargetVisualizer, targetVisualizer;

    public ArmSubsystem(MessengerClient msg, IntakeSubsystem intake) {
        this.intake = intake;

        bottom = new ArmJoint(CANAllocation.ARM_BOTTOM_MOTOR, CANAllocation.ARM_BOTTOM_CANCODER, CANCODER_TO_ARM_RATIO, BOTTOM_GEAR_RATIO, ARM_BOTTOM_OFFSET, true);
        top = new ArmJoint(CANAllocation.ARM_TOP_MOTOR, CANAllocation.ARM_TOP_CANCODER, CANCODER_TO_ARM_RATIO, TOP_GEAR_RATIO, ARM_TOP_OFFSET, false);
        wrist = new WristJoint(CANAllocation.ARM_WRIST_MOTOR, CANAllocation.ARM_WRIST_CANCODER, WRIST_CANCODER_TO_ARM_RATIO, WRIST_GEAR_RATIO, ARM_WRIST_OFFSET, false);

        double size = (BOTTOM_LENGTH + TOP_LENGTH + WRIST_RAD) * 2;
        Mechanism2d visualizer = new Mechanism2d(size, size);
        targetVisualizer = new ArmVisualizer(size/2, size/2, visualizer, "Target", Color.kDarkRed, Color.kRed, Color.kOrangeRed);
        stepTargetVisualizer = new ArmVisualizer(size/2, size/2, visualizer, "Step Target", Color.kDarkOrange, Color.kOrange, Color.kDarkGoldenrod);
        currentVisualizer = new ArmVisualizer(size/2, size/2, visualizer, "Current", Color.kDarkGreen, Color.kGreen, Color.kLightGreen);
        SmartDashboard.putData("Arm Visualizer", visualizer);

        ArmPose home = ArmPositions.DEFAULT.get().toPose();
        System.out.println("Home pose: " + home);
        if (home == null)
            throw new IllegalStateException("Home position must be valid!");
        bottom.calibratePosition(home.bottomAngle);
        top.calibratePosition(home.topAngle.ccw().wrapDeg(-270, 90));
        wrist.calibratePosition(home.wristAngle.sub(home.topAngle));

        pathfinder = new ArmPathfinder(msg);
        movePid = NTUtil.tunablePID(ARM_MOVE_KP, ARM_MOVE_KI, ARM_MOVE_KD);
        targetPose = home;

        ARM_BRAKE_MODE.nowAndOnChange((brake) -> {
            bottom.setBrakeMode(brake);
            top.setBrakeMode(brake);
            wrist.setBrakeMode(brake);
        });

        wristFold = ARM_FOLD_ANGLE_CUBE;
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

    @Override
    public void periodic() {
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
        double startTol = ARM_START_TOL.get();
        double stopTol = ARM_STOP_TOL.get();
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
            pidOut = MathUtil.clamp(pidOut, 0, ARM_MAX_SPEED.get());

            // Apply bias to towardsTarget so that each axis takes equal time
            // This allows the assumption in the pathfinder that moving towards
            // a target travels in a straight line in state space
            towardsTarget.mul(ArmConstants.BOTTOM_GEAR_RATIO, ArmConstants.TOP_GEAR_RATIO)
                    .boxNormalize().mul(pidOut);

            if (Double.isNaN(towardsTarget.x) || Double.isNaN(towardsTarget.y)) {
                throw new RuntimeException("Towards target vector is NaN somehow");
            }

            // Set motor outputs to move towards the current target
            bottom.setMotorOutput(towardsTarget.x);
            top.setMotorOutput(towardsTarget.y);
        }

        // Always fold regardless of having a game piece, since we can't
        // reliably determine if we have one. This is fine without game
        // piece since the intake fits over the drive base at all angles.
        Vec2d axisPos = currentPose.getAxisPos().absolute();
        Vec2d foldZone = ARM_FOLD_ZONE.get();
        Angle wristTarget = targetPose.wristAngle;
        Angle wristRef = targetPose.topAngle;
        NTEntry<Angle> foldAngle = intake.getHeldPiece() == GamePiece.CUBE ? ARM_FOLD_ANGLE_CUBE : ARM_FOLD_ANGLE_CONE;
        if (axisPos.x <= foldZone.x && axisPos.y <= foldZone.y) {
            // Set wrist to fold angle
            wristTarget = wristFold.get();
            wristRef = currentPose.topAngle;
        } else {
            // Only update fold angle when not inside fold zone
            // This is because switching fold angles causes intake to collide
            // with drive base
            wristFold = foldAngle;
        }
        Logger.getInstance().recordOutput("Wrist/Abs Target (ccw deg)", wristTarget.ccw().deg());
        Logger.getInstance().recordOutput("Wrist/Abs Current (ccw deg)", currentPose.wristAngle.ccw().deg());

        // Calculate the feedforward needed to counteract gravity on the wrist
        double wristFF = wristTarget.ccw().sin() * ARM_WRIST_FULL_HOLD.get();

        wristTarget = wristTarget.sub(wristRef).ccw().wrapDeg(-180, 180);
        wrist.setTargetAngle(wristTarget, wristFF);
    }

    public void setTargetPosition(ArmPosition targetPosition) {
        if (Double.isNaN(targetPosition.axisPos.x) || Double.isNaN(targetPosition.axisPos.y)) {
            throw new RuntimeException("Target position is NaN somehow");
        }
        ArmPose pose = targetPosition.toPose();
        if (pose == null) {
            System.err.println("Trying to set arm to invalid position");
            return;
        }
        if (Double.isNaN(pose.bottomAngle.ccw().deg()) || Double.isNaN(pose.topAngle.ccw().deg()) || Double.isNaN(pose.wristAngle.ccw().deg())) {
            throw new RuntimeException("Target pose is NaN somehow");
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
