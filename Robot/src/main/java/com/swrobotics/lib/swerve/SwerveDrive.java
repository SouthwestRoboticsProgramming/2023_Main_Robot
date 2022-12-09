package com.swrobotics.lib.swerve;

import com.swrobotics.lib.schedule.Subsystem;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.XboxController;

public class SwerveDrive implements Subsystem {
    private final XboxController controller = new XboxController(0); // FIXME: Remove

    private final SwerveModule[] modules = {
        new SwerveModule(5, 1, 9, -38.936 + 90, new Translation2d(-1, 1)), // Front left
        new SwerveModule(6, 3, 10, -77.520 + 90, new Translation2d(1, 1)),  // Front right
        new SwerveModule(7, 2, 11, -95.449 + 90, new Translation2d(-1, -1)),  // Back left
        new SwerveModule(8, 4, 12, -224.209 + 90, new Translation2d(1, -1)),  // Back right
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
        ChassisSpeeds speeds = ChassisSpeeds.fromFieldRelativeSpeeds(driveX, driveY, turn, new Rotation2d());
        SwerveModuleState[] targetStates = kinematics.toSwerveModuleStates(speeds);

        for (int i = 0; i < modules.length; i++) {
            SwerveModuleState state = targetStates[i];
            modules[i].set(state.speedMetersPerSecond, state.angle.getRadians());
        }
    }

    @Override
    public void periodic() {
        drive(
            -controller.getLeftY(), 
            -controller.getLeftX(), 
            -controller.getRightX());
    }
}
