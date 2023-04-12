package com.swrobotics.taskmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class TaskManager {
    private static final Type TASKS_MAP_TYPE = new TypeToken<Map<String, Task>>() {}.getType();

    private static final File CONFIG_FILE = new File("config.json");
    private static final File TASKS_FILE = new File("tasks.json");

    private final Gson tasksGson;
    private final TaskManagerAPI api;
    private final Map<String, Task> tasks;

    public TaskManager() {
        TaskManagerConfiguration config = TaskManagerConfiguration.load(CONFIG_FILE);
        api = new TaskManagerAPI(this, config);
        tasksGson =
                new GsonBuilder()
                        .registerTypeAdapter(File.class, new FileTypeAdapter())
                        .registerTypeAdapter(Task.class, new TaskSerializer(api, config))
                        .setPrettyPrinting()
                        .create();

        tasks = loadTasks();

        for (Map.Entry<String, Task> entry : tasks.entrySet()) {
            Task task = entry.getValue();
            task.setName(entry.getKey());
            task.start();
        }

        saveTasks();
    }

    private Map<String, Task> loadTasks() {
        // If the file doesn't exist, there must not be any tasks yet
        if (!TASKS_FILE.exists()) return new HashMap<>();

        try {
            return tasksGson.fromJson(new FileReader(TASKS_FILE), TASKS_MAP_TYPE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load tasks file", e);
        }
    }

    private void saveTasks() {
        try {
            FileWriter writer = new FileWriter(TASKS_FILE);
            tasksGson.toJson(tasks, writer);
            writer.close();
        } catch (Exception e) {
            System.err.println("Failed to save tasks file");
            e.printStackTrace();
        }
    }

    public void addTask(Task task) {
        tasks.put(task.getName(), task);
        saveTasks();
    }

    public Task getTask(String name) {
        return tasks.get(name);
    }

    public void removeTask(String name) {
        Task removed = tasks.remove(name);
        if (removed != null) {
            removed.forceStop();
            saveTasks();
        }
    }

    public Map<String, Task> getTasks() {
        return new HashMap<>(tasks);
    }

    public void run() {
        while (true) {
            api.read();
            for (Task task : tasks.values()) {
                task.restartIfProcessEnded();
            }

            try {
                Thread.sleep(1000 / 50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
