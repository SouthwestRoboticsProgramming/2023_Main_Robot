package com.swrobotics.shufflelog.tool.blockauto;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.tool.Tool;
import com.swrobotics.shufflelog.tool.ToolConstants;
import com.swrobotics.shufflelog.util.Cooldown;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static imgui.ImGui.begin;
import static imgui.ImGui.beginPopupContextItem;
import static imgui.ImGui.beginPopupModal;
import static imgui.ImGui.beginTable;
import static imgui.ImGui.button;
import static imgui.ImGui.closeCurrentPopup;
import static imgui.ImGui.end;
import static imgui.ImGui.endPopup;
import static imgui.ImGui.endTable;
import static imgui.ImGui.getWindowHeight;
import static imgui.ImGui.getWindowViewport;
import static imgui.ImGui.getWindowWidth;
import static imgui.ImGui.inputText;
import static imgui.ImGui.openPopup;
import static imgui.ImGui.popID;
import static imgui.ImGui.pushID;
import static imgui.ImGui.selectable;
import static imgui.ImGui.separator;
import static imgui.ImGui.setItemDefaultFocus;
import static imgui.ImGui.setNextItemWidth;
import static imgui.ImGui.setWindowPos;
import static imgui.ImGui.tableNextColumn;
import static imgui.ImGui.text;
import static imgui.ImGui.textDisabled;

public final class BlockAutoTool implements Tool {
    public static final String BLOCK_DND_ID = "AB_DRAG_BLOCK";
    public static BlockAutoTool INSTANCE;

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

    private final MessengerClient msg;

    private final Cooldown blockDefsQueryCooldown;
    private final List<BlockCategory> categories;
    private boolean receivedCategories;
    private final Map<String, BlockDef> blockDefs;

    private final Cooldown sequencesQueryCooldown;
    private final List<String> sequences;
    private boolean receivedSequences;

    private final Cooldown sequenceDataQueryCooldown;

    private final ImString popupInput;

    private String activeSeqName;
    private BlockStackInst activeSeqStack;

    public BlockAutoTool(ShuffleLog log) {
        INSTANCE = this;

        msg = log.getMessenger();
        msg.addHandler(MSG_BLOCK_DEFS, this::onBlockDefs);
        msg.addHandler(MSG_SEQUENCES, this::onSequences);
        msg.addHandler(MSG_SEQUENCE_DATA, this::onSequenceData);
        msg.addHandler(MSG_PUBLISH_CONFIRM, this::onPublishConfirm);
        msg.addHandler(MSG_DELETE_CONFIRM, this::onDeleteConfirm);

        msg.addDisconnectHandler(() -> {
            receivedSequences = false;
            receivedCategories = false;
            activeSeqName = null;
            activeSeqStack = null;
        });

        blockDefsQueryCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);
        categories = new ArrayList<>();
        receivedCategories = false;
        blockDefs = new HashMap<>();

        sequencesQueryCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);
        sequences = new ArrayList<>();
        receivedSequences = false;

        sequenceDataQueryCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);

        popupInput = new ImString(64);

        activeSeqName = null;
        activeSeqStack = null;
    }

    private void onBlockDefs(String type, MessageReader reader) {
        int count = reader.readInt();
        categories.clear();
        for (int i = 0; i < count; i++) {
            BlockCategory cat = BlockCategory.read(reader);
            for (BlockInst inst : cat.getBlocks()) {
                BlockDef def = inst.getDef();
                blockDefs.put(def.getName(), def);
            }
            categories.add(cat);
        }
        receivedCategories = true;
    }

    private void onSequences(String type, MessageReader reader) {
        int len = reader.readInt();
        sequences.clear();
        for (int i = 0; i < len; i++) {
            sequences.add(reader.readString());
        }
        receivedSequences = true;
    }

    private void onSequenceData(String type, MessageReader reader) {
        String name = reader.readString();
        if (!name.equals(activeSeqName)) return;

        boolean valid = reader.readBoolean();
        if (!valid) {
            System.err.println("Block auto: Invalid sequence data");
            return;
        }

        activeSeqStack = BlockStackInst.read(reader, this);
    }

    private void onPublishConfirm(String type, MessageReader reader) {
        String name = reader.readString();

        // If this is a new sequence, initialize and open it
        if (!sequences.contains(name)) {
            sequences.add(name);

            activeSeqName = name;
            activeSeqStack = null;
        }
    }

    private void onDeleteConfirm(String type, MessageReader reader) {
        String name = reader.readString();
        boolean success = reader.readBoolean();

        if (!success)
            return;

        sequences.remove(name);
        if (activeSeqName.equals(name)) {
            activeSeqName = null;
            activeSeqStack = null;
        }
    }

    public BlockDef getBlockDef(String name) {
        return blockDefs.get(name);
    }

    public void onStackChange() {
        MessageBuilder builder = msg.prepare(MSG_PUBLISH_SEQUENCE_DATA)
                .addString(activeSeqName);

        activeSeqStack.write(builder);

        builder.send();
    }

    private void switchSequence(String sequence) {
        activeSeqName = sequence;
        activeSeqStack = null;
    }

    private String getPopupInput() {
        byte[] data = popupInput.getData();
        StringBuilder builder = new StringBuilder();
        for (byte b : data) {
            if (b == 0)
                break;
            else
                builder.append((char) b);
        }
        return builder.toString();
    }

    private boolean inputStringPopup(String title, String prompt, String confirm) {
        if (beginPopupModal(title, ImGuiWindowFlags.NoMove)) {
            // Center the popup
            ImGuiViewport vp = getWindowViewport();
            setWindowPos(vp.getCenterX() - getWindowWidth() / 2, vp.getCenterY() - getWindowHeight() / 2);

            text(prompt);
            setNextItemWidth(300);
            boolean submit = inputText("##name", popupInput, ImGuiInputTextFlags.EnterReturnsTrue);
            setItemDefaultFocus();

            setNextItemWidth(300);
            submit |= button(confirm);

            if (submit)
                closeCurrentPopup();

            endPopup();

            return submit;
        }
        return false;
    }

    private void showSequenceList() {
        text("Sequences");
        separator();

        for (String sequence : sequences) {
            pushID(sequence);
            if (selectable(sequence, sequence.equals(activeSeqName))) {
                switchSequence(sequence);
            }
            if (beginPopupContextItem("context_menu")) {
                // TODO
//                if (selectable("Rename")) {
//                    closeCurrentPopup();
//                }
                if (selectable("Delete")) {
                    msg.prepare(MSG_DELETE_SEQUENCE)
                            .addString(sequence)
                            .send();
                }
                endPopup();
            }
            popID();
        }

        if (button("Add")) {
            popupInput.set("");
            openPopup("Add Sequence");
        }

        if (inputStringPopup("Add Sequence", "New sequence name:", "Create")) {
            // Create the sequence
            MessageBuilder builder = msg.prepare(MSG_PUBLISH_SEQUENCE_DATA)
                    .addString(getPopupInput());
            new BlockStackInst().write(builder);
            builder.send();
        }
    }

    private void showWorkArea() {
        if (activeSeqName == null) {
            textDisabled("No sequence open");
            return;
        }

        pushID("work_area");
        text(activeSeqName);
        separator();

        if (activeSeqStack == null) {
            textDisabled("Loading blocks...");
        } else {
            if (activeSeqStack.show())
                onStackChange();
        }
        popID();
    }

    private void showPalette() {
        pushID("palette");
        text("Block Palette");
        separator();

        for (BlockCategory cat : categories) {
            cat.draw();
        }
        popID();
    }

    @Override
    public void process() {
        if (begin("Block Auto")) {
            if (!msg.isConnected()) {
                ImGui.textDisabled("Not connected");
                end();
                return;
            }

            if (beginTable("layout", 3, ImGuiTableFlags.BordersInner | ImGuiTableFlags.Resizable)) {
                if (!receivedCategories && blockDefsQueryCooldown.request())
                    msg.send(MSG_QUERY_BLOCK_DEFS);
                if (!receivedSequences && sequencesQueryCooldown.request())
                    msg.send(MSG_QUERY_SEQUENCES);
                if (activeSeqName != null && activeSeqStack == null && sequenceDataQueryCooldown.request())
                    msg.prepare(MSG_GET_SEQUENCE_DATA).addString(activeSeqName).send();

                tableNextColumn();
                showSequenceList();
                tableNextColumn();
                showWorkArea();
                tableNextColumn();
                showPalette();

                endTable();
            }
        }
        end();
    }
}
