package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.lib.schedule.SwitchableSubsystemBase;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.robot.subsystems.arm.joint.ArmJoint;
import com.swrobotics.robot.subsystems.arm.joint.PhysicalJoint;
import com.swrobotics.robot.subsystems.arm.joint.SimJoint;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import org.littletonrobotics.junction.Logger;

import static com.swrobotics.robot.subsystems.arm.ArmConstants.*;

// All arm kinematics is treated as a 2d coordinate system, with
// the X axis representing forward, and the Y axis representing up
// FIXME: Wrist is currently not functional
// TODO: Redo pretty much all of this
public final class ArmSubsystemOld extends SwitchableSubsystemBase {
    // Info relating to each physical arm, since they aren't identical
    private static final NTBoolean OFFSET_CALIBRATE = new NTBoolean("Arm/Calibrate Offsets", false);

    private enum PhysicalArmInfo {
        // CANCoders should be calibrated to be at zero when arm is at home position
        ARM_1("Arm/Arm 1/Bottom Offset", "Arm/Arm 1/Top Offset"),
        ARM_2("Arm/Arm 2/Bottom Offset", "Arm/Arm 2/Top Offset");

        public final NTDouble bottomOffset;
        public final NTDouble topOffset;

        PhysicalArmInfo(String bottomPath, String topPath) {
            bottomOffset = new NTDouble(bottomPath, 10.283203125);
            topOffset = new NTDouble(topPath, -51.943359375);
        }
    }

    private static final int BOTTOM_MOTOR_ID = 23;
    private static final int TOP_MOTOR_ID = 24;

    // FIXME
    private static final int BOTTOM_CANCODER_ID = 13;
    private static final int TOP_CANCODER_ID = 14;

    public static final double JOINT_TO_CANCODER_RATIO = 2;

    private static final NTDouble MAX_SPEED = new NTDouble("Arm/Max Speed", 1.0);
    private static final NTDouble STOP_TOL = new NTDouble("Arm/Stop Tolerance", 0.01);
    private static final NTDouble START_TOL = new NTDouble("Arm/Start Tolerance", 0.04); // Must be larger than stop
                                                                                         // tolerance

    private static final NTBoolean HOME_CALIBRATE = new NTBoolean("Arm/Home/Calibrate", false);
    private static final NTDouble HOME_BOTTOM = new NTDouble("Arm/Home/Bottom", 0.5 * Math.PI);
    private static final NTDouble HOME_TOP = new NTDouble("Arm/Home/Top", -0.5 * Math.PI);

    private static final NTDouble KP = new NTDouble("Arm/PID/kP", 8);
    private static final NTDouble KI = new NTDouble("Arm/PID/kI", 0);
    private static final NTDouble KD = new NTDouble("Arm/PID/kD", 0);

    private final ArmJoint topJoint, bottomJoint;
    private final ArmPathfinder finder;
    private final PIDController pid;
    private ArmPose targetPose;
    private boolean inToleranceHysteresis;

    private final ArmVisualizer currentVisualizer;
    private final ArmVisualizer targetVisualizer;

    public ArmSubsystemOld(MessengerClient msg) {
        finder = new ArmPathfinder(msg);

        if (RobotBase.isSimulation()) {
            bottomJoint = new SimJoint(BOTTOM_LENGTH, BOTTOM_GEAR_RATIO);
            topJoint = new SimJoint(TOP_LENGTH, TOP_GEAR_RATIO);
        } else {
            // DigitalInput armDetect = new DigitalInput(RIOPorts.ARM_DETECT_DIO);
            // PhysicalArmInfo armInfo = armDetect.get() ? PhysicalArmInfo.ARM_1 :
            // PhysicalArmInfo.ARM_2;
            PhysicalArmInfo armInfo = PhysicalArmInfo.ARM_1;

            bottomJoint = new PhysicalJoint(
                    BOTTOM_MOTOR_ID,
                    BOTTOM_CANCODER_ID,
                    BOTTOM_GEAR_RATIO,
                    armInfo.bottomOffset,
                    true);
            topJoint = new PhysicalJoint(
                    TOP_MOTOR_ID,
                    TOP_CANCODER_ID,
                    TOP_GEAR_RATIO,
                    armInfo.topOffset,
                    false);
        }

        double extent = (BOTTOM_LENGTH + TOP_LENGTH) * 2;
        Mechanism2d mechanism = new Mechanism2d(extent, extent);
        currentVisualizer = new ArmVisualizer(
                extent / 2,
                extent / 2,
                mechanism,
                "Current Arm",
                Color.kDarkGreen,
                Color.kGreen, Color.kAqua);
        targetVisualizer = new ArmVisualizer(
                extent / 2,
                extent / 2,
                mechanism,
                "Target Arm",
                Color.kDarkRed,
                Color.kRed, Color.kCoral);
        SmartDashboard.putData("Arm", mechanism);

        // finder = new ArmPathfinder(msg);

//        ArmPose home = new ArmPose(HOME_BOTTOM.get(), HOME_TOP.get(), 0);
//        calibrateHome(home);
//        targetPose = home;
        inToleranceHysteresis = false;

        pid = new PIDController(KP.get(), KI.get(), KD.get());
        KP.onChange(() -> pid.setP(KP.get()));
        KI.onChange(() -> pid.setI(KI.get()));
        KD.onChange(() -> pid.setD(KD.get()));

        msg.addHandler(
                "Debug:ArmSetTarget",
                (type, reader) -> {
                    double x = reader.readDouble();
                    double y = reader.readDouble();
                    setTargetPosition(new Translation2d(x, y));
                });
    }

    private void calibrateHome(ArmPose homePose) {
        bottomJoint.calibrateHome(homePose.bottomAngle);
        topJoint.calibrateHome(homePose.topAngle);
    }

    public ArmPose getCurrentPose() {
        return new ArmPose(bottomJoint.getCurrentAngle(), topJoint.getCurrentAngle(), 0);
    }

    public Translation2d getHomeTarget() {
        return new ArmPose(HOME_BOTTOM.get(), HOME_TOP.get(), 0).getEndPosition();
    }

    private void idle() {
        // LOG_MOTOR_BOTTOM.set(0.0);
        // LOG_MOTOR_TOP.set(0.0);
        bottomJoint.setMotorOutput(0);
        topJoint.setMotorOutput(0);
    }

    @Override
    public void periodic() {
        ArmPose currentPose = getCurrentPose();

        // Check if it should home
//        if (HOME_CALIBRATE.get()) {
//            HOME_BOTTOM.set(currentPose.bottomAngle);
//            HOME_TOP.set(currentPose.topAngle);
//            HOME_CALIBRATE.set(false);
//        }

        // Check if it should home CANCoders
        if (OFFSET_CALIBRATE.get()) {
            OFFSET_CALIBRATE.set(false);

            // Assume arm is physically in home position
            bottomJoint.calibrateCanCoder();
            topJoint.calibrateCanCoder();
        }

        currentVisualizer.setPose(currentPose);

//        Logger.getInstance().recordOutput("arm/current/bottomAngle", currentPose.bottomAngle);
//        Logger.getInstance().recordOutput("arm/current/topAngle", currentPose.topAngle);

        Logger.getInstance().recordOutput("arm/inToleranceHysteresis", inToleranceHysteresis);

        if (targetPose == null) {
            idle();
            return;
        }

        targetVisualizer.setPose(targetPose);

        double startTol = START_TOL.get();
        double stopTol = STOP_TOL.get();

        ArmPose currentTarget = null;
        Vec2d currentPoseVec = toStateSpaceVec(currentPose);

        currentTarget = targetPose;

        Logger.getInstance().recordOutput("arm/target/bottomAngle", currentTarget.bottomAngle);
        Logger.getInstance().recordOutput("arm/target/topAngle", currentTarget.topAngle);

        double topAngle = MathUtil.wrap(currentTarget.topAngle + Math.PI, 0, Math.PI * 2) - Math.PI;

        Vec2d towardsTarget = new Vec2d(currentTarget.bottomAngle, topAngle)
                .sub(currentPose.bottomAngle, currentPose.topAngle);

        // Tolerance hysteresis so the motor doesn't do the shaky shaky
        double magSqToFinalTarget = toStateSpaceVec(targetPose).sub(currentPoseVec).magnitudeSq();
        boolean prevInTolerance = inToleranceHysteresis;

        Logger.getInstance().recordOutput("arm/magSqToFinalTarget", Math.sqrt(magSqToFinalTarget));

        if (magSqToFinalTarget > startTol * startTol) {
            inToleranceHysteresis = false;
        } else if (magSqToFinalTarget < stopTol * stopTol) {
            inToleranceHysteresis = true;
        }

        if (prevInTolerance && !inToleranceHysteresis)
            pid.reset();

        double bottomMotorOut, topMotorOut;
        if (inToleranceHysteresis) {
            bottomMotorOut = 0;
            topMotorOut = 0;
        } else {
            // PID towards final target so we don't slow down at each point
            double pidOut = -pid.calculate(Math.sqrt(magSqToFinalTarget), 0);
            pidOut = MathUtil.clamp(pidOut, 0, MAX_SPEED.get());

            towardsTarget.mul(BOTTOM_GEAR_RATIO, TOP_GEAR_RATIO).boxNormalize().mul(pidOut);
            bottomMotorOut = towardsTarget.x;
            topMotorOut = towardsTarget.y;
        }

        bottomJoint.setMotorOutput(bottomMotorOut);
        topJoint.setMotorOutput(topMotorOut);

        Logger.getInstance().recordOutput("arm/target/bottomOutput", bottomMotorOut);
        Logger.getInstance().recordOutput("arm/target/topOutput", topMotorOut);
    }

    @Override
    protected void onDisable() {
        bottomJoint.setMotorOutput(0);
        topJoint.setMotorOutput(0);
    }

    private static final NTEntry<Double> L_TARGET_X = new NTDouble("Log/Arm/Target X", 0).setTemporary();
    private static final NTEntry<Double> L_TARGET_Y = new NTDouble("Log/Arm/Target Y", 0).setTemporary();

    public void setTargetPosition(Translation2d position) {
        L_TARGET_X.set(position.getX());
        L_TARGET_Y.set(position.getY());
        targetPose = ArmPose.fromEndPosition(position, 0);
        inToleranceHysteresis = false;
    }

    public ArmPose getTargetPose() {
        return targetPose;
    }

    public boolean isInTolerance() {
        if (targetPose == null)
            return true;

        Vec2d currentPoseVec = toStateSpaceVec(getCurrentPose());
        double magSqToFinalTarget = toStateSpaceVec(targetPose).sub(currentPoseVec).magnitudeSq();

        double tol = STOP_TOL.get();
        return magSqToFinalTarget < tol * tol;
    }
}
