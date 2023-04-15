package com.swrobotics.taskmanager;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.File;
import java.lang.reflect.Type;

public final class TaskSerializer implements JsonSerializer<Task>, JsonDeserializer<Task> {
    private final TaskManagerAPI api;
    private final TaskManagerConfiguration config;

    public TaskSerializer(TaskManagerAPI api, TaskManagerConfiguration config) {
        this.api = api;
        this.config = config;
    }

    @Override
    public Task deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        File workingDir = context.deserialize(obj.get("workingDirectory"), File.class);
        String[] command = context.deserialize(obj.get("command"), String[].class);
        boolean enabled = obj.get("enabled").getAsBoolean();
        return new Task(workingDir, command, enabled, api, config.getMaxFailCount());
    }

    @Override
    public JsonElement serialize(Task src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.add("workingDirectory", context.serialize(src.getWorkingDirectory()));
        obj.add("command", context.serialize(src.getCommand()));
        obj.addProperty("enabled", src.isEnabled());
        return obj;
    }
}
