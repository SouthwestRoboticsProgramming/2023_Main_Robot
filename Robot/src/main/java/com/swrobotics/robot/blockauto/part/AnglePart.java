package com.swrobotics.robot.blockauto.part;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.CWAngle;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

import java.util.HashMap;
import java.util.Map;

public final class AnglePart extends ParamPart {
    public enum Mode {
        CW_DEG(0) {
            @Override
            public Angle toAngle(double val) {
                return CWAngle.deg(val);
            }

            @Override
            public double fromAngle(Angle val) {
                return val.cw().deg();
            }
        },
        CCW_DEG(1) {
            @Override
            public Angle toAngle(double val) {
                return CCWAngle.deg(val);
            }

            @Override
            public double fromAngle(Angle val) {
                return val.ccw().deg();
            }
        },
        CW_RAD(2) {
            @Override
            public Angle toAngle(double val) {
                return CWAngle.rad(val);
            }

            @Override
            public double fromAngle(Angle val) {
                return val.cw().rad();
            }
        },
        CCW_RAD(3) {
            @Override
            public Angle toAngle(double val) {
                return CCWAngle.rad(val);
            }

            @Override
            public double fromAngle(Angle val) {
                return val.ccw().rad();
            }
        },
        CW_ROT(4) {
            @Override
            public Angle toAngle(double val) {
                return CWAngle.rot(val);
            }

            @Override
            public double fromAngle(Angle val) {
                return val.cw().rot();
            }
        },
        CCW_ROT(5) {
            @Override
            public Angle toAngle(double val) {
                return CCWAngle.rot(val);
            }

            @Override
            public double fromAngle(Angle val) {
                return val.ccw().rot();
            }
        };

        private static final Map<Integer, Mode> BY_ID = new HashMap<>();
        static {
            for (Mode mode : values()) {
                BY_ID.put(mode.id, mode);
            }
        }

        private final int id;

        Mode(int id) {
            this.id = id;
        }

        public abstract Angle toAngle(double val);
        public abstract double fromAngle(Angle val);
    }

    private final Mode mode;
    private final double def;

    public AnglePart(String name, Mode mode, double def) {
        super(name);
        this.mode = mode;
        this.def = def;
    }

    @Override
    public Object readInst(MessageReader reader) {
        double val = reader.readDouble();
        return mode.toAngle(val);
    }

    @Override
    public void writeInst(MessageBuilder builder, Object val) {
        builder.addDouble(mode.fromAngle((Angle) val));
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(PartTypes.ANGLE.getId());
        
        builder.addByte((byte) mode.id);
        builder.addDouble(def);
    }

    @Override
    public Object deserializeInst(JsonElement elem) {
        if (elem == null) return def;
        return mode.toAngle(elem.getAsDouble());
    }

    @Override
    public JsonElement serializeInst(Object val) {
        return new JsonPrimitive(mode.fromAngle((Angle) val));
    }
}
