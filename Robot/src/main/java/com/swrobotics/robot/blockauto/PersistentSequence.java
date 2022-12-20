package com.swrobotics.robot.blockauto;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class PersistentSequence {
    private final String name;
    private final File file;
    private final BlockStackInst stack;

    public PersistentSequence(String name, File file) {
        this.name = name;
        this.file = file;
        try {
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

            System.out.println("Block auto: Loaded persistent sequence from '" + file.getName() + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        // Fake message builder so we can use same serialization code
        MessageBuilder builder = new MessageBuilder(null, null);
        stack.write(builder);
        
        byte[] data = builder.getData();
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(data);
            out.flush();
            out.close();

            System.out.println("Block auto: Saved persistent sequence to '" + file.getName() + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean delete() {
        return file.delete();
    }
}
