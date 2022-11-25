package com.swrobotics.messenger.server.log;

import com.swrobotics.messenger.server.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public final class FileLogger implements MessageLogger {
    private final long startTime;
    private final PrintWriter out;

    public FileLogger(File file, boolean compress) {
        try {
            if (!file.exists())
                file.createNewFile();

            OutputStream fileStream = new FileOutputStream(file);

            // If compressing log files, send data through GZIP
            if (compress) {
                fileStream = new GZIPOutputStream(fileStream);
            }

            out = new PrintWriter(fileStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Logging messages to " + file.getAbsolutePath());

        startTime = System.currentTimeMillis();

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::flush, 0, 1, TimeUnit.SECONDS);
    }

    private String getTimestamp() {
        return String.valueOf((System.currentTimeMillis() - startTime) / 1000.0);
    }

    @Override
    public void logEvent(String type, String name, String descriptor) {
        out.println(getTimestamp() + "\t_" + type + "\t" + name + "\t" + descriptor);
    }

    @Override
    public void logMessage(Message msg) {
        out.println(getTimestamp() + "\t" + msg.getType() + "\t" + Base64.getEncoder().encodeToString(msg.getData()));
    }

    private void flush() {
        out.flush();
    }

    public void close() {
        flush();
        out.close();
    }
}
