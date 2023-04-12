package com.swrobotics.taskmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

public final class TaskManagerConfiguration {
    public static Gson GSON =
            new GsonBuilder()
                    .registerTypeAdapter(File.class, new FileTypeAdapter())
                    .setPrettyPrinting()
                    .create();

    public static TaskManagerConfiguration load(File file) {
        try {
            return GSON.fromJson(new FileReader(file), TaskManagerConfiguration.class);
        } catch (FileNotFoundException e) {
            TaskManagerConfiguration conf = new TaskManagerConfiguration();

            System.err.println("Config file not found, saving default");
            try {
                FileWriter writer = new FileWriter(file);
                GSON.toJson(conf, writer);
                writer.close();
            } catch (Exception e2) {
                System.err.println("Failed to save default config file");
                e2.printStackTrace();
            }

            return conf;
        }
    }

    private String messengerHost = "localhost";
    private int messengerPort = 5805;
    private String messengerName = "TaskManager";
    private File tasksRoot = new File("tasks");
    private int maxFailCount = 10;

    private TaskManagerConfiguration() {}

    public String getMessengerHost() {
        return messengerHost;
    }

    public int getMessengerPort() {
        return messengerPort;
    }

    public String getMessengerName() {
        return messengerName;
    }

    public File getTasksRoot() {
        return tasksRoot;
    }

    public int getMaxFailCount() {
        return maxFailCount;
    }
}
