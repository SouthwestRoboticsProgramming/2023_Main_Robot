package com.swrobotics.robot.blockauto;

import com.google.gson.*;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.robot.RobotContainer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class BlockStackInst {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BlockStackInst.class, new BlockStackInst.Serializer())
            .registerTypeAdapter(BlockInst.class, new BlockInst.Serializer())
            .setPrettyPrinting()
            .create();

    public static BlockStackInst readFromMessenger(MessageReader reader) {
        int len = reader.readInt();
        BlockStackInst inst = new BlockStackInst();

        for (int i = 0; i < len; i++) {
            String name = reader.readString();
            BlockDef def = AutoBlocks.getBlockDef(name);
            if (def != null)
                inst.blocks.add(def.readInstance(reader));
            else
                System.err.println("Block is not defined: " + name);
        }

        return inst;
    }

    private final List<BlockInst> blocks;

    public BlockStackInst() {
        blocks = new ArrayList<>();
    }

    public void addBlock(BlockInst block) {
        blocks.add(block);
    }
    
    public void write(MessageBuilder builder) {
        builder.addInt(blocks.size());
        for (BlockInst block : blocks) {
            block.write(builder);
        }
    }

    public Command toCommand(RobotContainer robot) {
        if (blocks.size() == 1)
            return blocks.get(0).toCommand(robot);

        SequentialCommandGroup seq = new SequentialCommandGroup();
        for (BlockInst block : blocks) {
            seq.addCommands(block.toCommand(robot));
        }
        return seq;
    }

    public static final class Serializer implements JsonSerializer<BlockStackInst>, JsonDeserializer<BlockStackInst> {
        @Override
        public BlockStackInst deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            BlockStackInst stack = new BlockStackInst();

            JsonArray arr = json.getAsJsonArray();
            for (JsonElement elem : arr) {
                BlockInst block = context.deserialize(elem, BlockInst.class);
                if (block != null)
                    stack.addBlock(block);
            }

            return stack;
        }

        @Override
        public JsonElement serialize(BlockStackInst src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray arr = new JsonArray();
            for (BlockInst block : src.blocks) {
                arr.add(context.serialize(block));
            }
            return arr;
        }
    }
}
