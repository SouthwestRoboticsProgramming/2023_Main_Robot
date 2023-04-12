package com.swrobotics.shufflelog;

import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.profiler.Profiler;
import com.swrobotics.shufflelog.tool.ConeOrCubeTool;
import com.swrobotics.shufflelog.tool.MenuBarTool;
import com.swrobotics.shufflelog.tool.PreMatchChecklistTool;
import com.swrobotics.shufflelog.tool.Tool;
import com.swrobotics.shufflelog.tool.arm.ArmDebugTool;
import com.swrobotics.shufflelog.tool.data.DataLogTool;
import com.swrobotics.shufflelog.tool.data.nt.NetworkTablesTool;
import com.swrobotics.shufflelog.tool.field.FieldViewTool;
import com.swrobotics.shufflelog.tool.messenger.MessengerTool;
import com.swrobotics.shufflelog.tool.profile.ShuffleLogProfilerTool;
import com.swrobotics.shufflelog.tool.taskmanager.RoboRIOFilesTool;
import com.swrobotics.shufflelog.tool.taskmanager.TaskManagerTool;
import edu.wpi.first.math.WPIMathJNI;
import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.util.CombinedRuntimeLoader;
import edu.wpi.first.util.WPIUtilJNI;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import processing.core.PApplet;
import processing.core.PFont;

public final class ShuffleLog extends PApplet {
  private static final String LAYOUT_FILE = "layout.ini";
  private static final String PERSISTENCE_FILE = "persistence.properties";
  private static final int THREAD_POOL_SIZE = 4;

  private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
  private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
  private ImPlotContext imPlotCtx;

  private final List<Tool> tools = new ArrayList<>();
  private final List<Tool> addedTools = new ArrayList<>();
  private final List<Tool> removedTools = new ArrayList<>();

  // Things shared between tools
  private Properties persistence;
  private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
  private MessengerClient messenger;

  private long startTime;

  @Override
  public void settings() {
    NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);
    WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
    WPIMathJNI.Helper.setExtractOnStaticLoad(false);
    try {
      CombinedRuntimeLoader.loadLibraries(
          ShuffleLog.class, "wpiutiljni", "wpimathjni", "ntcorejni");
    } catch (IOException e) {
      throw new RuntimeException("Failed to load WPILib libraries", e);
    }

    persistence = new Properties();
    try {
      persistence.load(new FileReader(PERSISTENCE_FILE));
    } catch (IOException e) {
      System.err.println("Failed to load persistence file, using defaults");
      e.printStackTrace();
    }

    int width = Integer.parseInt(persistence.getProperty("window.width", "1280"));
    int height = Integer.parseInt(persistence.getProperty("window.height", "720"));
    size(width, height, P2D);
  }

  private void saveDefaultLayout() {
    File defaultLayoutFile = new File(LAYOUT_FILE);
    if (!defaultLayoutFile.exists()) {
      try {
        InputStream in = getClass().getClassLoader().getResourceAsStream(LAYOUT_FILE);
        OutputStream out = new FileOutputStream(defaultLayoutFile);

        if (in == null) throw new IOException("Failed to load default layout resource");

        byte[] buf = new byte[1024];
        int read;
        while ((read = in.read(buf)) > 0) out.write(buf, 0, read);

        in.close();
        out.close();
      } catch (IOException e) {
        System.err.println("Failed to save default layout file:");
        e.printStackTrace();
      }
    }
  }

  @Override
  public void setup() {
    saveDefaultLayout();

    surface.setResizable(true);
    long windowHandle = (long) surface.getNative();

    ImGui.createContext();
    imPlotCtx = ImPlot.createContext();

    ImGuiIO io = ImGui.getIO();
    io.setIniFilename(LAYOUT_FILE);
    io.setConfigFlags(ImGuiConfigFlags.DockingEnable);
    Styles.applyDark();

    imGuiGlfw.init(windowHandle, true);
    imGuiGl3.init();

    // Set default font
    try {
      textFont(
          new PFont(
              getClass().getClassLoader().getResourceAsStream("fonts/PTSans-Regular-14.vlw")));
    } catch (IOException e) {
      e.printStackTrace();
    }

    tools.add(new MenuBarTool());
    MessengerTool msg = new MessengerTool(this);
    tools.add(msg);
    tools.add(new ShuffleLogProfilerTool(this));
    DataLogTool dataLog = new DataLogTool(this);
    tools.add(dataLog);
    tools.add(new NetworkTablesTool(threadPool));
    tools.add(new TaskManagerTool(this, "TaskManager"));
    tools.add(new RoboRIOFilesTool(this));
    tools.add(new FieldViewTool(this));
    tools.add(new ArmDebugTool(this, messenger));
    tools.add(new PreMatchChecklistTool(msg));
    tools.add(new ConeOrCubeTool(messenger));

    startTime = System.currentTimeMillis();
  }

  @Override
  public void draw() {
    Profiler.beginMeasurements("Root");

    if (messenger != null) {
      Profiler.push("Read Messages");
      messenger.readMessages();
      Profiler.pop();
    }

    Profiler.push("Begin GUI frame");
    imGuiGlfw.flushEvents();
    imGuiGlfw.newFrame();
    ImGui.newFrame();
    ImGuizmo.beginFrame();
    Profiler.pop();

    background(210);
    ImGui.dockSpaceOverViewport();

    for (Tool tool : tools) {
      Profiler.push(tool.getClass().getSimpleName());

      // if (going to crash) { dont(); }
      try {
        tool.process();
      } catch (Throwable t) {
        // Log it and ignore
        t.printStackTrace();
      }

      Profiler.pop();
    }
    tools.addAll(addedTools);
    tools.removeAll(removedTools);
    addedTools.clear();
    removedTools.clear();

    Profiler.push("Render GUI");
    Profiler.push("Flush");
    flush();
    Profiler.pop();
    Profiler.push("Render draw data");
    ImGui.render();
    imGuiGl3.renderDrawData(ImGui.getDrawData());
    Profiler.pop();
    Profiler.pop();

    Profiler.endMeasurements();
  }

  @Override
  public void exit() {
    messenger.disconnect();

    ImPlot.destroyContext(imPlotCtx);
    ImGui.destroyContext();

    try {
      persistence.store(new FileWriter(PERSISTENCE_FILE), "ShuffleLog persistent data");
    } catch (IOException e) {
      System.err.println("Failed to save persistent data");
      e.printStackTrace();
    }

    super.exit();
  }

  @Override
  public void keyPressed() {
    // Prevent closing on escape key press
    if (key == ESC) key = 0;
  }

  public void addTool(Tool tool) {
    addedTools.add(tool);
  }

  public void removeTool(Tool tool) {
    removedTools.add(tool);
  }

  public double getTimestamp() {
    return (System.currentTimeMillis() - startTime) / 1000.0;
  }

  public MessengerClient getMessenger() {
    return messenger;
  }

  public void setMessenger(MessengerClient messenger) {
    this.messenger = messenger;
  }

  public static void main(String[] args) {
    PApplet.main(ShuffleLog.class);
  }
}
