package com.swrobotics.robot;

import edu.wpi.first.math.filter.LinearFilter;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public class PoseFilter {
    private final LinearFilter xFilter;
    private final LinearFilter yFilter;
    private final LinearFilter angleFilter;

    public PoseFilter(int taps) {
        xFilter = LinearFilter.movingAverage(taps);
        yFilter = LinearFilter.movingAverage(taps);
        angleFilter = LinearFilter.movingAverage(taps);
    }

    public Pose2d get(Pose2d unfiltered) {
        return new Pose2d(
            new Translation2d(
                xFilter.calculate(unfiltered.getX()),
                yFilter.calculate(unfiltered.getY())
            ),
             // FIXME: Filter rotation - MUST BE CONTINUOUS
            unfiltered.getRotation()
        );
    }
}
