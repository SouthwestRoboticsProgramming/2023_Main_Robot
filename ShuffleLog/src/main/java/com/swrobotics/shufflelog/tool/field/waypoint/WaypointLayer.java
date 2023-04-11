package com.swrobotics.shufflelog.tool.field.waypoint;

import static processing.core.PConstants.CENTER;

import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.math.Vector2f;
import com.swrobotics.shufflelog.tool.ToolConstants;
import com.swrobotics.shufflelog.tool.field.FieldLayer;
import com.swrobotics.shufflelog.tool.field.FieldViewTool;
import com.swrobotics.shufflelog.util.Cooldown;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import java.util.ArrayList;
import java.util.List;
import processing.core.PGraphics;

public final class WaypointLayer implements FieldLayer {
  public static WaypointLayer INSTANCE = null;

  public static final String MSG_GET_WAYPOINTS = "Waypoints:Get";
  public static final String MSG_ADD_WAYPOINT = "Waypoints:Add";
  public static final String MSG_REMOVE_WAYPOINT = "Waypoints:Remove";
  public static final String MSG_WAYPOINTS = "Waypoints:List";

  private final FieldViewTool tool;
  private final MessengerClient msg;
  private final Cooldown cooldown;

  private final ImBoolean show;
  private final ImString nameInput;

  private final List<Waypoint> waypoints;
  private boolean hasWaypoints;
  private Waypoint selection, hover;
  private boolean isSelectingPoint;

  public WaypointLayer(FieldViewTool tool, MessengerClient msg) {
    if (INSTANCE != null)
      throw new IllegalStateException("Waypoint layer instantiated multiple times");
    INSTANCE = this;

    this.tool = tool;
    this.msg = msg;
    cooldown = new Cooldown(ToolConstants.MSG_CONSTANT_QUERY_COOLDOWN_TIME);

    msg.addHandler(MSG_WAYPOINTS, this::onWaypoints);

    show = new ImBoolean(true);
    nameInput = new ImString(64);

    waypoints = new ArrayList<>();
    hasWaypoints = false;
    selection = null;
    hover = null;
    isSelectingPoint = false;

    msg.addDisconnectHandler(
        () -> {
          waypoints.clear();
          hasWaypoints = false;
          selection = null;
          hover = null;
          isSelectingPoint = false;
        });
  }

  public void onWaypoints(String type, MessageReader reader) {
    int count = reader.readInt();
    waypoints.clear();
    for (int i = 0; i < count; i++) {
      String name = reader.readString();
      double x = reader.readDouble();
      double y = reader.readDouble();
      boolean editable = reader.readBoolean();
      waypoints.add(new Waypoint(x, y, name, editable));
    }

    if (selection != null) {
      // Re-find selection

      String selName = selection.getName();
      selection = null;

      for (Waypoint wp : waypoints) {
        if (selName.equals(wp.getName())) {
          selection = wp;
        }
      }

      if (selection == null) {
        isSelectingPoint = false;
      }
    }

    hasWaypoints = true;
  }

  @Override
  public String getName() {
    return "Waypoints";
  }

  @Override
  public void processAlways() {
    if (cooldown.request()) msg.send(MSG_GET_WAYPOINTS);
  }

  @Override
  public void draw(PGraphics g) {
    if (!msg.isConnected()) return;

    if (!show.get()) return;

    g.ellipseMode(CENTER);
    g.strokeWeight(6);
    for (Waypoint wp : waypoints) {
      if (wp == selection) g.stroke(255, 128, 0);
      else if (wp == hover) g.stroke(128, 255, 128);
      else g.stroke(255);

      float x = (float) wp.getX().get();
      float y = (float) wp.getY().get();
      g.point(x, y);
    }
  }

  private void fancyLabel(String label) {
    ImGui.tableNextColumn();
    ImGui.text(label);
    ImGui.tableNextColumn();
    ImGui.setNextItemWidth(-1);
  }

  private boolean waypointExistsWithName(String name, Waypoint exclusion) {
    for (Waypoint wp : waypoints) {
      if (wp == exclusion) continue;

      if (wp.getName().equals(name)) return true;
    }
    return false;
  }

  private String uniqueify(String name, Waypoint exclusion) {
    String testName = name;
    int i = 1;
    while (true) {
      if (!waypointExistsWithName(testName, exclusion)) return testName;

      i++;
      testName = name + " (" + i + ")";
    }
  }

  @Override
  public void showGui() {
    ImGui.checkbox("Show", show);
    ImGui.separator();

    if (!msg.isConnected()) {
      ImGui.textDisabled("Not connected");
      return;
    }

    if (ImGui.button("Refresh")) {
      hasWaypoints = false;
      waypoints.clear();
      selection = null;
      hover = null;
      isSelectingPoint = false;
    }

    if (ImGui.beginTable("##wp_list", 1, ImGuiTableFlags.Borders)) {
      hover = null;
      for (Waypoint wp : new ArrayList<>(waypoints)) {
        ImGui.tableNextColumn();
        if (ImGui.selectable(wp.getName(), wp == selection)) {
          selection = wp;
          isSelectingPoint = false;
        }
        if (ImGui.isItemHovered()) {
          hover = wp;
        }
        if (ImGui.beginPopupContextItem()) {
          if (ImGui.selectable("Delete")) {
            wp.delete(msg);
            waypoints.remove(wp);
            if (selection == wp) {
              selection = null;
              isSelectingPoint = false;
            }
          }
          ImGui.endPopup();
        }
      }
      ImGui.endTable();
      ImGui.beginDisabled(!hasWaypoints);
      if (ImGui.button("Add new waypoint")) {
        Waypoint newWp = new Waypoint(0, 0, uniqueify("New Waypoint", null), true);
        selection = newWp;
        waypoints.add(newWp);
        newWp.add(msg);
        isSelectingPoint = true;
      }
      ImGui.endDisabled();
    }

    if (selection != null) {
      ImGui.separator();
      if (ImGui.beginTable("conn_layout", 2, ImGuiTableFlags.SizingStretchProp)) {
        String oldName = selection.getName();
        nameInput.set(oldName);

        boolean changed, nameChanged;
        ImGui.beginDisabled(!selection.isEditable());
        fancyLabel("Name");
        changed = nameChanged = ImGui.inputText("##name", nameInput);
        fancyLabel("X");
        changed |= ImGui.inputDouble("##x", selection.getX());
        fancyLabel("Y");
        changed |= ImGui.inputDouble("##y", selection.getY());
        ImGui.endDisabled();

        if (changed && selection.isEditable()) {
          selection.delete(msg);
          if (nameChanged) selection.setName(uniqueify(nameInput.get(), selection));
          selection.add(msg);
        }

        ImGui.endTable();
      }
      ImGui.setNextItemWidth(-1);
      if (ImGui.button("Select new position")) {
        isSelectingPoint = true;
      }
    }

    if (isSelectingPoint) ImGui.text("Click on field to set position");

    if (isSelectingPoint && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
      Vector2f where = tool.getCursorPos();
      if (where != null) {
        selection.getX().set(where.x);
        selection.getY().set(where.y);
        selection.delete(msg);
        selection.add(msg);
      }
      isSelectingPoint = false;
    }
  }

  public List<Waypoint> getWaypoints() {
    if (!hasWaypoints) return null;
    return waypoints;
  }
}
