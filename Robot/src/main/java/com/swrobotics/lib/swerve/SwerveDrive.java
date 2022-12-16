package com.swrobotics.lib.swerve;

import com.kauailabs.navx.frc.AHRS;
import com.swrobotics.lib.schedule.Subsystem;
import com.swrobotics.mathlib.CoordinateConversions;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Joystick;

public class SwerveDrive implements Subsystem {
    private final Joystick controller = new Joystick(0); // FIXME: Remove
    private final AHRS navx = new AHRS();

    private final SwerveModule[] modules = {
        new SwerveModule(5, 9, 1, -44.648, CoordinateConversions.toWPICoords(new Vec2d(-1, 1))), // Front left
        new SwerveModule(6, 10, 2, -95.273 + 180, CoordinateConversions.toWPICoords(new Vec2d(1, 1))),  // Front right
        new SwerveModule(7, 11, 3, -256.992, CoordinateConversions.toWPICoords(new Vec2d(-1, -1))),  // Back left
        new SwerveModule(8, 12, 4, -38.320 + 180, CoordinateConversions.toWPICoords(new Vec2d(1, -1))),  // Back right
    };

    private final SwerveDriveKinematics kinematics;

    public SwerveDrive() {
        Translation2d[] positions = new Translation2d[modules.length];
        for (int i = 0; i < modules.length; i++) {
            positions[i] = modules[i].position;
        }
        kinematics = new SwerveDriveKinematics(positions);
    }

    public void printEncoderOffsets() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < modules.length; i++) {
            builder.append("M");
            builder.append(i);
            builder.append(" ");

            builder.append(String.format("%.3f", modules[i].getCurrentEncoderAngle()));
            builder.append(" ");
        }
        System.out.println(builder);
    }

    public void drive(double driveX, double driveY, double turn) {
        Rotation2d gyro = Rotation2d.fromDegrees(-navx.getYaw());

        ChassisSpeeds speeds = ChassisSpeeds.fromFieldRelativeSpeeds(driveX, driveY, turn, gyro);
        SwerveModuleState[] targetStates = kinematics.toSwerveModuleStates(speeds);

        for (int i = 0; i < modules.length; i++) {
            SwerveModuleState state = targetStates[i];
            modules[i].set(state.speedMetersPerSecond, state.angle.getRadians());
        }
    }

    int count = 50;

    @Override
    public void teleopInit() {
        navx.zeroYaw();
    }

    @Override
    public void periodic() {
        count--;
        if (count == 0) {
            count = 100;
            System.out.println("Encoder positions:");
            printEncoderOffsets();
        }
    }

    boolean prevCalibrate = false;

    @Override
    public void teleopPeriodic() {
        boolean calibrate = controller.getRawButton(7);
        if (calibrate && !prevCalibrate)
            navx.zeroYaw();
        prevCalibrate = calibrate;

        double driveScale = 0.3;
        double turnScale = 0.2;

        Translation2d translation = CoordinateConversions.toWPICoords(new Vec2d(
            MathUtil.deadband(controller.getX(), 0.1) * driveScale,
            -MathUtil.deadband(controller.getY(), 0.1) * driveScale
        ));

        drive(
            translation.getX(),
            translation.getY(),
            MathUtil.deadband(-controller.getZ(), 0.2) * turnScale
        );
    }
}
