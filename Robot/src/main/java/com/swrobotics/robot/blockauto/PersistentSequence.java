package com.swrobotics.robot.blockauto;

import com.swrobotics.messenger.client.MessageReader;
import edu.wpi.first.wpilibj.DriverStation;

import java.io.*;

public final class PersistentSequence {
    private final String name;
    private final File file;
    private final BlockStackInst stack;

    public PersistentSequence(String name, File file) throws IOException {
        this.name = name;

        // Convert old .auto files to .json
        if (file.getName().endsWith(AutoBlocks.PERSISTENCE_FILE_EXT_OLD)) {
            DriverStation.reportWarning("Converting old .auto file: " + file.getName(), false);

            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) > 0) {
                b.write(buffer, 0, read);
            }
            in.close();

            MessageReader reader = new MessageReader(b.toByteArray());
            stack = BlockStackInst.readFromMessenger(reader);

            this.file = new File(file.getParent(), name + AutoBlocks.PERSISTENCE_FILE_EXT);
            save();
            file.delete();
        } else {
            this.file = file;
            try (FileReader reader = new FileReader(file)) {
                stack = BlockStackInst.GSON.fromJson(reader, BlockStackInst.class);
            }
        }

        System.out.println("Block auto: Loaded persistent sequence from '" + file.getName() + "'");
    }

    public PersistentSequence(String name, File file, BlockStackInst stack) {
        this.name = name;
        this.file = file;
        this.stack = stack;
    }

    public String getName() {
        return name;
    }

    public BlockStackInst getStack() {
        return stack;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            BlockStackInst.GSON.toJson(stack, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean delete() {
        return file.delete();
    }
}
