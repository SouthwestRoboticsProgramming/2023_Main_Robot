package com.swrobotics.shufflelog.tool.profile;

import com.swrobotics.profiler.MemoryStats;
import com.swrobotics.profiler.ProfileNode;
import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.tool.Tool;
import com.swrobotics.shufflelog.tool.data.DoubleDataPlot;
import com.swrobotics.shufflelog.tool.data.Graph;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class ProfilerTool implements Tool {
  private static final String SEPARATOR = "/";

  private final ShuffleLog log;
  private final String name;
  private final Graph memoryGraph;

  private String selectionPath;

  public ProfilerTool(ShuffleLog log, String name) {
    this.name = name;
    this.log = log;

    memoryGraph = new Graph("Memory (MB)");
    memoryGraph.addPlot(
        new DoubleDataPlot("Total", "", 10) {
          @Override
          protected Double read() {
            return (double) getMemStats().getTotal() / MemoryStats.BYTES_PER_MB;
          }
        });
    memoryGraph.addPlot(
        new DoubleDataPlot("Used", "", 10) {
          @Override
          protected Double read() {
            return (double) getMemStats().getUsed() / MemoryStats.BYTES_PER_MB;
          }
        });

    selectionPath = "";
  }

  protected abstract ProfileNode getLastData();

  protected abstract MemoryStats getMemStats();

  private void sortNodes(List<ProfileNode> nodes) {
    nodes.sort(Comparator.comparingLong((n) -> -n.getTotalTimeNanoseconds()));
  }

  private static final double RADIANS_PER_POLY = Math.PI / 16;

  private void drawPieSlice(
      ImDrawList draw,
      double angleMin,
      double angleMax,
      float centerX,
      float centerY,
      double radius,
      int color) {
    int polyCount = (int) Math.ceil((angleMax - angleMin) / RADIANS_PER_POLY);

    ImVec2[] points = new ImVec2[polyCount + 2];
    points[0] = new ImVec2(centerX, centerY);
    for (int i = 0; i <= polyCount; i++) {
      double angle = angleMin + (i / (double) polyCount) * (angleMax - angleMin);
      points[i + 1] =
          new ImVec2(
              centerX + (float) (radius * Math.cos(angle)),
              centerY + (float) (radius * Math.sin(angle)));
    }

    draw.addConvexPolyFilled(points, points.length, color);
  }

  private static final int[] PIE_COLORS = {
    ImGui.colorConvertFloat4ToU32(1, 0, 0, 1),
    ImGui.colorConvertFloat4ToU32(1, 1, 0, 1),
    ImGui.colorConvertFloat4ToU32(0, 1, 0, 1),
    ImGui.colorConvertFloat4ToU32(0, 1, 1, 1),
    ImGui.colorConvertFloat4ToU32(0, 0, 1, 1),
    ImGui.colorConvertFloat4ToU32(1, 0, 1, 1)
  };

  private void showSelectionPie(ProfileNode root) {
    // Find selected node
    ProfileNode node = root;
    if (selectionPath.length() > 0) {
      String[] parts = selectionPath.substring(1).split(SEPARATOR);
      partLoop:
      for (String part : parts) {
        for (ProfileNode child : node.getChildren()) {
          if (child.getName().equals(part)) {
            node = child;
            continue partLoop;
          }
        }
        break;
      }
    }

    List<ProfileNode> children = new ArrayList<>(node.getChildren());
    long unspecifiedTime = node.getSelfTimeNanoseconds();
    children.add(new ProfileNode("unspecified", null, unspecifiedTime, unspecifiedTime));
    sortNodes(children);

    // Ideally this would use ImPlot, but there is a bug in the native
    // binding that prevents it from working
    ImGui.text("Selected: " + (selectionPath.length() == 0 ? "/" : selectionPath));
    if (ImGui.beginTable("pie_layout", 2)) {
      ImGui.tableNextColumn();
      ImGui.invisibleButton("pie", 150, 150);
      ImVec2 min = ImGui.getItemRectMin();
      ImVec2 max = ImGui.getItemRectMax();
      ImDrawList draw = ImGui.getWindowDrawList();
      draw.pushClipRect(min.x, min.y, max.x, max.y, true);
      long totalTime = 0;
      for (ProfileNode child : children) totalTime += child.getTotalTimeNanoseconds();
      double angle = 0;
      int i = 0;
      for (ProfileNode child : children) {
        double angle2 =
            children.size() == 1
                ? Math.PI * 2
                : angle + (double) child.getTotalTimeNanoseconds() / totalTime * Math.PI * 2;
        drawPieSlice(
            draw, angle, angle2, min.x + 75, min.y + 75, 75, PIE_COLORS[i % PIE_COLORS.length]);
        angle = angle2;
        i++;
      }
      draw.popClipRect();

      ImGui.tableNextColumn();
      i = 0;
      for (ProfileNode child : children) {
        ImVec4 color = new ImVec4();
        ImGui.colorConvertU32ToFloat4(PIE_COLORS[i % PIE_COLORS.length], color);
        ImGui.colorButton(
            child.getName(),
            new float[] {color.x, color.y, color.z, color.w},
            ImGuiColorEditFlags.NoTooltip,
            15,
            15);
        ImGui.sameLine();
        ImGui.text(child.getName());
        i++;
      }

      ImGui.endTable();
    }
  }

  private void showNode(ProfileNode node, String path, boolean isRoot) {
    if (!isRoot) path = path + SEPARATOR + node.getName();

    int flags =
        ImGuiTreeNodeFlags.SpanFullWidth
            | ImGuiTreeNodeFlags.OpenOnArrow
            | ImGuiTreeNodeFlags.OpenOnDoubleClick;
    List<ProfileNode> children = node.getChildren();
    if (children.isEmpty()) flags |= ImGuiTreeNodeFlags.Leaf;
    if (path.equals(selectionPath)) flags |= ImGuiTreeNodeFlags.Selected;
    if (isRoot) flags |= ImGuiTreeNodeFlags.DefaultOpen;

    sortNodes(children);

    ImGui.tableNextRow();
    ImGui.tableNextColumn();
    boolean open = ImGui.treeNodeEx(node.getName(), flags);
    if (ImGui.isItemClicked()) selectionPath = path;
    ImGui.tableNextColumn();
    ImGui.text(String.format("%.3f", node.getSelfTimeNanoseconds() / 1_000_000.0f));
    ImGui.tableNextColumn();
    ImGui.text(String.format("%.3f", node.getTotalTimeNanoseconds() / 1_000_000.0f));

    if (open) {
      for (ProfileNode child : children) {
        showNode(child, path, false);
      }
      ImGui.treePop();
    }
  }

  protected abstract void showHeader();

  @Override
  public void process() {
    memoryGraph.sample(log.getTimestamp());

    if (ImGui.begin(name)) {
      ImGui.setWindowSize(475, 600, ImGuiCond.FirstUseEver);

      showHeader();

      ProfileNode node = getLastData();
      if (node == null) {
        ImGui.text("No data yet...");
        ImGui.end();
        return;
      }

      memoryGraph.plot();
      ImGui.separator();

      showSelectionPie(node);
      ImGui.separator();

      int flags =
          ImGuiTableFlags.BordersV
              | ImGuiTableFlags.BordersOuterH
              | ImGuiTableFlags.Resizable
              | ImGuiTableFlags.RowBg;

      if (ImGui.beginTable("nodes", 3, flags)) {
        ImVec2 size = new ImVec2();
        ImGui.calcTextSize(size, "A");
        float w = size.x;

        ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WidthStretch);
        ImGui.tableSetupColumn("Self (ms)", ImGuiTableColumnFlags.WidthFixed, 120);
        ImGui.tableSetupColumn("Total (ms)", ImGuiTableColumnFlags.WidthFixed, 120);
        ImGui.tableHeadersRow();

        showNode(node, "", true);
        ImGui.endTable();
      }
    }
    ImGui.end();
  }
}
