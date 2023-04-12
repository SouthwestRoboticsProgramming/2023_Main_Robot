package com.swrobotics.shufflelog.tool;

import com.swrobotics.shufflelog.tool.messenger.MessengerTool;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTreeNodeFlags;
import java.util.function.Supplier;

public final class PreMatchChecklistTool implements Tool {
  private static final class Entry {
    private final String name;
    private final String[] description;
    private Supplier<Boolean> checker;

    public Entry(String name, String... description) {
      this.name = name;
      this.description = description;
    }

    public Entry withChecker(Supplier<Boolean> checker) {
      this.checker = checker;
      return this;
    }
  }

  private final Entry[] entries;
  private boolean hasSetFocus;
  private boolean open;

  public PreMatchChecklistTool(MessengerTool msg) {
    entries =
        new Entry[] {
          new Entry("Is the driver station open?"),
          new Entry("Is the driver controller connected in port 0?"),
          new Entry("Is the manipulator controller connected in port 1?"),
          //                new Entry("Is the button panel connected?")
          //                        .withChecker(btn::isConnected),
          new Entry(
                  "Is Messenger connected to the robot?",
                  "Make sure the host is '10.21.29.3' and port is 5805.")
              .withChecker(msg::isConnectedToRobot),
          new Entry(
              "Have you selected the correct auto?",
              "Use the auto chooser in ShuffleBoard for this.",
              "If selecting Block Auto, you must select a sequence in 'Auto/Sequence'"
                  + " in NetworkTables."),
          new Entry("Are you behind the white line?")
        };

    hasSetFocus = false;
    open = true;
  }

  @Override
  public void process() {
    if (!open) return;

    if (ImGui.begin("Pre-Match Checklist")) {
      if (!hasSetFocus) ImGui.setWindowFocus(); // Always be on top
      hasSetFocus = true;

      for (Entry entry : entries) {
        int flags;
        if (entry.description.length == 0)
          flags = ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen;
        else flags = ImGuiTreeNodeFlags.DefaultOpen;

        boolean open = ImGui.treeNodeEx(entry.name, flags);
        if (entry.checker != null) {
          ImGui.sameLine();

          boolean checked = entry.checker.get();
          if (checked) {
            ImGui.pushStyleColor(ImGuiCol.Text, 0.0f, 1.0f, 0.0f, 1.0f);
            ImGui.text("YES");
          } else {
            ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.0f, 0.0f, 1.0f);
            ImGui.text("NO");
          }
          ImGui.popStyleColor();
        }

        if (!open || entry.description.length == 0) continue;

        for (String desc : entry.description) {
          ImGui.treeNodeEx(desc, ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen);
        }

        ImGui.treePop();
      }

      ImGui.separator();
      if (ImGui.button("Ready to Start Match", -1, 0)) {
        open = false;
      }
    }
    ImGui.end();
  }
}
