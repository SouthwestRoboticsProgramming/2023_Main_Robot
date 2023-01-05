package com.swrobotics.robot.blockauto;

import com.google.gson.*;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.blockauto.part.BlockPart;
import com.swrobotics.robot.blockauto.part.ParamPart;
import edu.wpi.first.wpilibj2.command.Command;

import java.lang.reflect.Type;

/**
 * An instance of a {@link BlockDef}.
 */
public final class BlockInst {
    private final BlockDef def;
    private final Object[] params;

    public BlockInst(BlockDef def, Object... params) {
        this.def = def;
        this.params = params;
    }

    public void write(MessageBuilder builder) {
        builder.addString(def.getName());
        int paramIdx = 0;
        for (BlockPart part : def.getParts()) {
            if (part instanceof ParamPart) {
                ParamPart p = (ParamPart) part;
                p.writeInst(builder, params[paramIdx++]);
            }
        }
    }

    public Command toCommand(RobotContainer robot) {
        return def.getCreator().apply(params, robot);
    }

    public static final class Serializer implements JsonSerializer<BlockInst>, JsonDeserializer<BlockInst> {
        @Override
        public BlockInst deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            String type = obj.get("$type").getAsString();
            BlockDef def = AutoBlocks.getBlockDef(type);
            if (def != null)
                return def.deserializeInstance(obj);
            else
                return null;
        }

        @Override
        public JsonElement serialize(BlockInst src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("$type", src.def.getName());
            int paramIdx = 0;
            for (BlockPart part : src.def.getParts()) {
                if (part instanceof ParamPart) {
                    ParamPart p = (ParamPart) part;
                    obj.add(p.getName(), p.serializeInst(src.params[paramIdx++]));
                }
            }
            return obj;
        }
    }
}
