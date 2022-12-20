package com.swrobotics.robot.blockauto;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.robot.RobotContainer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import java.util.ArrayList;
import java.util.List;

public final class BlockStackInst {
    public static BlockStackInst readFromMessenger(MessageReader reader) {
        int len = reader.readInt();
        BlockStackInst inst = new BlockStackInst();

        for (int i = 0; i < len; i++) {
            String name = reader.readString();
            BlockDef def = AutoBlocks.getBlockDef(name);
            inst.blocks.add(def.readInstance(reader));
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
}
