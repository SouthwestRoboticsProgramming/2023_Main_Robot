package com.swrobotics.shufflelog.tool.blockauto.part;

import com.swrobotics.messenger.client.MessageReader;

public final class FieldPointPart extends Vec2dPart {
    public static FieldPointPart read(MessageReader reader) {
        double x = reader.readDouble();
        double y = reader.readDouble();
        return new FieldPointPart(x, y);
    }

    public FieldPointPart(double defX, double defY) {
        super(defX, defY);
    }
}
