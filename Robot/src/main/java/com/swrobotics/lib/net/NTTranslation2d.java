package com.swrobotics.lib.net;

import edu.wpi.first.math.geometry.Translation2d;

public final class NTTranslation2d extends NTDoubleArray {
  public NTTranslation2d(String path, double defX, double defY) {
    super(path, defX, defY);
  }

  public Translation2d getTranslation() {
    double[] coords = get();
    return new Translation2d(coords[0], coords[1]);
  }

  public void set(Translation2d tx) {
    set(tx.getX(), tx.getY());
  }

  public void set(double x, double y) {
    set(new double[] {x, y});
  }
}
