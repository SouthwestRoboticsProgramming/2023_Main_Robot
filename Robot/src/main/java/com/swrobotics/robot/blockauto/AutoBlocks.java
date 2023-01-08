package com.swrobotics.robot.blockauto;

import com.swrobotics.lib.net.NTMultiSelect;
import com.swrobotics.lib.swerve.commands.DriveBlindCommand;
import com.swrobotics.lib.swerve.commands.PathfindToPointCommand;
import com.swrobotics.lib.swerve.commands.TurnBlindCommand;
import com.swrobotics.lib.swerve.commands.TurnToAngleCommand;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.blockauto.part.AnglePart.Mode;
import com.swrobotics.robot.blockauto.part.FieldPointPart.Point;
import com.swrobotics.robot.commands.AutoBalanceCommand;
import com.swrobotics.robot.subsystems.Lights;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.Vec2d;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AutoBlocks {
    // Dropdown menu in ShuffleLog to select which auto to run
    private static final NTMultiSelect<PersistentSequence> SELECTED_AUTO = new NTMultiSelect<>("Auto/Sequence", null) {
        @Override
        protected PersistentSequence valueOf(String name) {
            if (!sequences.containsKey(name))
                throw new IllegalArgumentException();
            return sequences.get(name);
        }

        @Override
        protected String nameOf(PersistentSequence seq) {
            return seq == null ? "[NULL]" : seq.getName();
        }
    };

    public static Command getSelectedAutoCommand() {
        PersistentSequence seq = SELECTED_AUTO.get();
        if (seq == null) return null;

        return seq.getStack().toCommand(robot);
    }

    private static void defineBlocks() {
        // The name parameter to newBlock("...") must be unique

        BlockCategory control = defineCategory("Control", 123, 73, 14);
        control.newBlock("wait")
                .text("Wait")
                .paramDouble("time", 1)
                .text("seconds")
                .creator((params, robot) -> new WaitCommand((double) params[0]));

        control.newBlock("union")
                 .text("Union of")
                 .paramBlockStack("a")
                 .text("and")
                 .paramBlockStack("b")
                 .creator((params, robot) -> new ParallelCommandGroup(
                         ((BlockStackInst) params[0]).toCommand(robot),
                         ((BlockStackInst) params[1]).toCommand(robot)
                 ));

        BlockCategory lights = defineCategory("Lights", 69, 39, 110);
        lights.newBlock("lights")
                .text("Set lights to ")
                .paramEnum("color", Lights.Color.class, Lights.Color.BLUE)
                .creator((params, robot) -> new CommandBase() {

                    @Override
                    public void initialize() {
                        System.out.println("Setting lights to " + (Lights.Color) params[0]);
                        robot.m_lights.set((Lights.Color) params[0]);
                    }

                    @Override
                    public boolean isFinished() {
                        return true;
                    }
                    
                });
        
        BlockCategory drive = defineCategory("Drive", 42, 57, 112);
        drive.newBlock("blind drive for time")
                .text("Drive at ")
                .paramDouble("speed", 1.0)
                .text(" MPS at ")
                .paramAngle("rotation", Mode.CW_DEG, 0.0)
                .text(" cw deg for ")
                .paramDouble("time", 1.0)
                .text(" seconds")
                .text("Robot relative: ")
                .paramBoolean("robot-relative", false)
                .creator((params, robot) -> new DriveBlindCommand(robot, (Angle) params[1], (double) params[0], (double) params[2], (boolean) params[3]));

        drive.newBlock("blind turn for time")
                .text("Turn at ")
                .paramDouble("rotation", 0.0)
                .text(" radians per second for ")
                .paramDouble("time", 1.0)
                .text(" seconds")
                .creator((params, robot) -> new TurnBlindCommand(robot, (double) params[0], (double) params[1]));

        drive.newBlock("turn to angle")
                .text("Turn to ")
                .paramAngle("target", Mode.CW_DEG, 0)
                .text(" cw deg")
                .text("Robot relative: ")
                .paramBoolean("robot-relative", false)
                .creator((params, robot) -> new TurnToAngleCommand(robot, (Angle) params[0], (boolean) params[1]));
    
        drive.newBlock("reset pose")
            .text("Reset pose to ")
            .paramVec2d("position", 0.0, 0.0)
            .text("(wpi)")
            .paramAngle("angle", Mode.CW_DEG, 0.0)
            .text("(cw deg)")
            .creator((params, robot) -> new CommandBase() {
                @Override
                public void initialize() {
                    System.out.println("Resetting pose");
                    
                    Vec2d translation = (Vec2d) params[0];
                    Angle rotation = (Angle) params[1];

                    Pose2d newPose = new Pose2d(
                        new Translation2d(translation.x, translation.y),
                        rotation.ccw().rotation2d()
                    );

                    // Reset gyro before reseting odometry to fix field oriented drive
                    robot.m_drivetrainSubsystem.setGyroscopeRotation(newPose.getRotation());
                    robot.m_drivetrainSubsystem.resetPose(newPose);
                }

                @Override
                public boolean isFinished() {
                    return true;
                }
            });

        drive.newBlock("balance")
            .text("Balance")
            .creator((params, robot) -> new AutoBalanceCommand(robot));

        drive.newBlock("pathfind to point")
                .text("Pathfind to ")
                .paramFieldPoint("goal", 0.0, 0.0)
                .creator((params, robot) -> new PathfindToPointCommand(robot, ((Point) params[0]).getPosition()));

        initRegistryAndValidate();
    }

    private static final List<BlockCategory> categories = new ArrayList<>();

    private static BlockCategory defineCategory(String name, int r, int g, int b) {
        BlockCategory cat = new BlockCategory(name, (byte) r, (byte) g, (byte) b);
        categories.add(cat);
        return cat;
    }

    private static void initRegistryAndValidate() {
        for (BlockCategory cat : categories) {
            System.out.println("C: " + cat);
            for (BlockDef block : cat.getBlocks()) {
                block.validate();

                if (blockDefRegistry.containsKey(block.getName()))
                    throw new IllegalStateException("Block definition validation failed: Duplicate name '" + block.getName() + "'");

                blockDefRegistry.put(block.getName(), block);
            }
        }
    }

    /*
     * Protocol:
     * 
     * S->R: Query block definitions
     * R->S: Block definitions (categories)
     * 
     * S->R: Query sequences
     * R->S: Sequences (string list)
     *
     * S->R: Delete sequence (string)
     * R->S: Delete confirm (same string)
     * S->R: Get sequence data (string)
     * R->S: Sequence data (block stack inst)
     * S->R: Publish sequence data (block stack inst)
     * R->S: Confirm publish
     * 
     * Storage:
     * 
     * Auto sequences are stored in the same format as the Messenger data to simplify code.
     * Stored in .auto files.
     */

    private static final String MSG_QUERY_BLOCK_DEFS      = "AutoBlock:QueryBlockDefs";
    private static final String MSG_QUERY_SEQUENCES       = "AutoBlock:QuerySequences";
    private static final String MSG_GET_SEQUENCE_DATA     = "AutoBlock:GetSequenceData";
    private static final String MSG_PUBLISH_SEQUENCE_DATA = "AutoBlock:PublishSequenceData";
    private static final String MSG_DELETE_SEQUENCE       = "AutoBlock:DeleteSequence";

    private static final String MSG_BLOCK_DEFS      = "AutoBlock:BlockDefs";
    private static final String MSG_SEQUENCES       = "AutoBlock:Sequences";
    private static final String MSG_SEQUENCE_DATA   = "AutoBlock:SequenceData";
    private static final String MSG_PUBLISH_CONFIRM = "AutoBlock:PublishConfirm";
    private static final String MSG_DELETE_CONFIRM  = "AutoBlock:DeleteConfirm";

    private static final File PERSISTENCE_DIR = new File(Filesystem.getOperatingDirectory(), "BlockAuto");
    public static final String PERSISTENCE_FILE_EXT = ".json";
    public static final String PERSISTENCE_FILE_EXT_OLD = ".auto";
    private static final Map<String, PersistentSequence> sequences = new HashMap<>();

    private static final Map<String, BlockDef> blockDefRegistry = new HashMap<>();
    private static MessengerClient msg;
    private static RobotContainer robot;

    public static BlockDef getBlockDef(String id) {
        return blockDefRegistry.get(id);
    }

    public static void init(MessengerClient msg, RobotContainer robot) {
        defineBlocks();

        if (!PERSISTENCE_DIR.exists() && !PERSISTENCE_DIR.mkdirs()) {
            DriverStation.reportWarning("Block auto: Failed to create persistence directory", false);
        }

        // Read in existing sequences
        File[] persistenceFiles = PERSISTENCE_DIR.listFiles();
        if (persistenceFiles != null) {
            for (File file : persistenceFiles) {
                String ext = PERSISTENCE_FILE_EXT;
                if (!file.getName().endsWith(PERSISTENCE_FILE_EXT)) {
                    ext = PERSISTENCE_FILE_EXT_OLD;
                    if (!file.getName().endsWith(PERSISTENCE_FILE_EXT_OLD))
                        continue;
                }

                String name = file.getName();
                name = name.substring(0, name.length() - ext.length());

                try {
                    PersistentSequence seq = new PersistentSequence(name, file);
                    sequences.put(name, seq);
                } catch (Throwable t) {
                    System.err.println("Failed to read block auto file: " + name);
                    t.printStackTrace();
                }
            }
        }
        updateSelectorOptions();

        AutoBlocks.msg = msg;
        AutoBlocks.robot = robot;
        msg.addHandler(MSG_QUERY_BLOCK_DEFS,      AutoBlocks::onQueryBlockDefs);
        msg.addHandler(MSG_QUERY_SEQUENCES,       AutoBlocks::onQuerySequences);
        msg.addHandler(MSG_GET_SEQUENCE_DATA,     AutoBlocks::onGetSequenceData);
        msg.addHandler(MSG_PUBLISH_SEQUENCE_DATA, AutoBlocks::onPublishSequenceData);
        msg.addHandler(MSG_DELETE_SEQUENCE,       AutoBlocks::onDeleteSequence);

        System.out.println("Block auto initialized");
    }

    private static void updateSelectorOptions() {
        List<PersistentSequence> options = new ArrayList<>(sequences.values());
        options.add(null); // Case to fall back to if the value is invalid

        SELECTED_AUTO.setOptions(options);
    }

    private static void onQueryBlockDefs(String type, MessageReader reader) {
        MessageBuilder builder = msg.prepare(MSG_BLOCK_DEFS);
        builder.addInt(categories.size());
        for (BlockCategory cat : categories) {
            cat.writeToMessenger(builder);
        }
        builder.send();
    }

    private static void onQuerySequences(String type, MessageReader reader) {
        MessageBuilder builder = msg.prepare(MSG_SEQUENCES);
        builder.addInt(sequences.size());
        for (String name : sequences.keySet()) {
            builder.addString(name);
        }
        builder.send();
    }

    private static void onGetSequenceData(String type, MessageReader reader) {
        String name = reader.readString();
        PersistentSequence sequence = sequences.get(name);

        MessageBuilder builder = msg.prepare(MSG_SEQUENCE_DATA);
        builder.addString(name);

        if (sequence == null) {
            builder.addBoolean(false);
            builder.send();
            return;
        }

        builder.addBoolean(true);
        sequence.getStack().write(builder);
        builder.send();
    }

    private static void onPublishSequenceData(String type, MessageReader reader) {
        String name = reader.readString();
        BlockStackInst inst = BlockStackInst.readFromMessenger(reader);

        PersistentSequence sequence = new PersistentSequence(
                name,
                new File(PERSISTENCE_DIR, name + PERSISTENCE_FILE_EXT),
                inst
        );
        sequences.put(name, sequence);
        sequence.save();
        updateSelectorOptions();

        msg.prepare(MSG_PUBLISH_CONFIRM)
                .addString(name)
                .send();
    }

    private static void onDeleteSequence(String type, MessageReader reader) {
        String name = reader.readString();

        PersistentSequence sequence = sequences.remove(name);
        boolean success = false;
        if (sequence != null) {
            success = sequence.delete();
        }
        updateSelectorOptions();

        msg.prepare(MSG_DELETE_CONFIRM)
                .addString(name)
                .addBoolean(success)
                .send();
    }
}
