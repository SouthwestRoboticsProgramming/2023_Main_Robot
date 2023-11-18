package com.swrobotics.shufflelog.tool;

import com.swrobotics.messenger.client.MessengerClient;

import edu.wpi.first.networktables.BooleanSubscriber;
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiColorEditFlags;

public final class ConeOrCubeTool implements Tool {
    // FIXME: VERY BAD
    public static double VALUE = -1000;

    private static final String MESSAGE = "Robot:GamePiece";

    private static final int CONE_COLOR = ImGui.getColorU32(1, 1, 0, 1);
    private static final int CUBE_COLOR = ImGui.getColorU32(0.25f, 0.25f, 1, 1);

    // Null is not connected
//    private Boolean isCone;
    private NetworkTableEntry isConeEnt;

    public ConeOrCubeTool(MessengerClient msg) {
        isConeEnt = null;


//        msg.addHandler(
//                MESSAGE,
//                (type, data) -> {
//                    isCone = data.readBoolean();
//                });
//
//        msg.addDisconnectHandler(() -> isCone = null);
    }

    public void updateNT(NetworkTableInstance instance) {
//        isConeSub = instance.getBooleanTopic("Is Cone").subscribe(false);
        isConeEnt = instance.getEntry("Random stuffs/Is Cone v2");
        System.out.println("UPDATE NTTTT");
    }

    public void stopNT() {
        isConeEnt = null;
        System.out.println("STOP NTTTTTTTTTTT");
    }

    @Override
    public void process() {
        if (ImGui.begin("Cone or Cube")) {
            double v;
            if (isConeEnt == null || (v = VALUE) < 0) {
                ImGui.textDisabled("Unknown");
                ImGui.end();
                return;
            }

            float[] color;
//            isConeEnt.setBoolean(Math.random() > 0.5);
            if (v > 0) color = new float[] {1, 1, 0, 1};
            else color = new float[] {0.25f, 0.25f, 1, 1};

            ImVec2 size = ImGui.getContentRegionAvail();
            ImGui.colorButton(
                    "##button",
                    color,
                    ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoDragDrop,
                    size.x,
                    size.y);
        }
        ImGui.end();
    }
}
