package com.swrobotics.robot.blockauto;

import com.google.gson.JsonObject;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.blockauto.part.AnglePart;
import com.swrobotics.robot.blockauto.part.BlockPart;
import com.swrobotics.robot.blockauto.part.BlockStackPart;
import com.swrobotics.robot.blockauto.part.BooleanPart;
import com.swrobotics.robot.blockauto.part.DoublePart;
import com.swrobotics.robot.blockauto.part.EnumPart;
import com.swrobotics.robot.blockauto.part.FieldPointPart;
import com.swrobotics.robot.blockauto.part.IntPart;
import com.swrobotics.robot.blockauto.part.NewLinePart;
import com.swrobotics.robot.blockauto.part.ParamPart;
import com.swrobotics.robot.blockauto.part.TextPart;
import com.swrobotics.robot.blockauto.part.Vec2dPart;
import edu.wpi.first.wpilibj2.command.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class BlockDef {
    private final String name;
    private final List<BlockPart> parts;
    private BiFunction<Object[], RobotContainer, Command> creator;

    public BlockDef(String name) {
        this.name = name;
        parts = new ArrayList<>();
    }

    public BlockDef text(String text) {
        parts.add(new TextPart(text));
        return this;
    }

    public BlockDef newLine() {
        parts.add(NewLinePart.INSTANCE);
        return this;
    }

    public BlockDef paramInt(String name, int def) {
        parts.add(new IntPart(name, def));
        return this;
    }

    public BlockDef paramDouble(String name, double def) {
        parts.add(new DoublePart(name, def));
        return this;
    }

    public BlockDef paramBoolean(String name, boolean def) {
        parts.add(new BooleanPart(name, def));
        return this;
    }

    public BlockDef paramAngle(String name, AnglePart.Mode mode, double def) {
        parts.add(new AnglePart(name, mode, def));
        return this;
    }

    public BlockDef paramVec2d(String name, double defX, double defY) {
        parts.add(new Vec2dPart(name, defX, defY));
        return this;
    }

    public BlockDef paramFieldPoint(String name, double defX, double defY) {
        parts.add(new FieldPointPart(name, defX, defY));
        return this;
    }

    public BlockDef paramBlockStack(String name) {
        parts.add(new BlockStackPart(name));
        return this;
    }

    public <E extends Enum<E>> BlockDef paramEnum(String name, Class<E> type, E def) {
        parts.add(new EnumPart<>(name, type, def));
        return this;
    }

    public void creator(BiFunction<Object[], RobotContainer, Command> creator) {
        this.creator = creator;
    }

    public void validate() {
        if (creator == null) {
            throw new IllegalStateException("Block definition validation failed: No command creator registered");
        }
    }

    public BlockInst readInstance(MessageReader reader) {
        List<Object> params = new ArrayList<>();
        for (BlockPart part : parts) {
            if (part instanceof ParamPart) {
                ParamPart p = (ParamPart) part;
                params.add(p.readInst(reader));
            }
        }
        return new BlockInst(this, params.toArray());
    }

    public BlockInst deserializeInstance(JsonObject obj) {
        List<Object> params = new ArrayList<>();
        for (BlockPart part : parts) {
            if (part instanceof ParamPart) {
                ParamPart p = (ParamPart) part;
                Object val;
                try {
                    val = p.deserializeInst(obj.get(p.getName()));
                } catch (Throwable t) {
                    System.err.println("Failed to deserialize block:");
                    t.printStackTrace();
                    val = p.deserializeInst(null);
                }
                params.add(val);
            }
        }
        return new BlockInst(this, params.toArray());
    }

    public void writeToMessenger(MessageBuilder builder) {
        builder.addString(name);
        builder.addInt(parts.size());
        for (BlockPart part : parts) {
            part.writeToMessenger(builder);
        }
    }

    public List<BlockPart> getParts() {
        return parts;
    }

    public String getName() {
        return name;
    }

    public BiFunction<Object[], RobotContainer, Command> getCreator() {
        return creator;
    }
}
