package com.swrobotics.robot.blockauto;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.blockauto.part.BlockPart;
import com.swrobotics.robot.blockauto.part.ParamPart;
import edu.wpi.first.wpilibj2.command.Command;

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
}
