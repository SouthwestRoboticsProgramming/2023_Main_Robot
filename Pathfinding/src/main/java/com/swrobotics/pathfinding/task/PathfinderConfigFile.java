package com.swrobotics.pathfinding.task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.pathfinding.field.Field;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Contains the fixed parameters for the pathfinder. These parameters are not adjustable via
 * Messenger.
 */
public final class PathfinderConfigFile {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static PathfinderConfigFile load(File file) {
        try {
            return GSON.fromJson(new FileReader(file), PathfinderConfigFile.class);
        } catch (FileNotFoundException e) {
            PathfinderConfigFile conf = new PathfinderConfigFile();

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

    private MessengerParams messenger = new MessengerParams();
    private FieldParams field = new FieldParams();
    private String gridFile = "grids.json";
    private FinderType finderType = FinderType.THETA_STAR;

    public static final class MessengerParams {
        private String host = "localhost";
        private int port = 5805;
        private String name = "Pathfinder";

        public MessengerParams() {}

        public MessengerClient createClient() {
            return new MessengerClient(host, port, name);
        }
    }

    public static final class FieldParams {
        // Standard FRC field size
        private double width = 8.2296;
        private double height = 16.4592;

        // Six inches per cell
        private double cellSize = 0.1524;

        // Origin in the center
        private double originX = 0.5;
        private double originY = 0.5;

        public FieldParams() {}

        public Field createField() {
            return new Field(cellSize, width, height, originX, originY);
        }
    }

    public PathfinderConfigFile() {}

    public MessengerParams getMessenger() {
        return messenger;
    }

    public FieldParams getField() {
        return field;
    }

    public String getGridFile() {
        return gridFile;
    }

    public FinderType getFinderType() {
        return finderType;
    }
}
