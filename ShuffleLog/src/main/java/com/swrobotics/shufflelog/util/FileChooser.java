package com.swrobotics.shufflelog.util;

import org.lwjgl.system.Platform;

import java.io.File;
import java.util.function.Consumer;

import javax.swing.*;

public final class FileChooser {
    public static void chooseFileOrFolder(Consumer<File> callback) {
        if (Platform.get() == Platform.MACOSX)
            throw new IllegalStateException("Cannot open Swing file chooser on MacOS with OpenGL");

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        JFrame frame = new JFrame("Choose a File or Folder");
        frame.setSize(480, 360);

        chooser.addActionListener(
                (event) -> {
                    if (event.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                        callback.accept(chooser.getSelectedFile());
                        frame.dispose();
                    } else if (event.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
                        frame.dispose();
                    }
                });

        frame.getContentPane().add(chooser);
        frame.setVisible(true);
    }

    private FileChooser() {
        throw new AssertionError();
    }
}
