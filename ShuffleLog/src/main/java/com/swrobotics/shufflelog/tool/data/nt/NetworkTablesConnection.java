package com.swrobotics.shufflelog.tool.data.nt;

import edu.wpi.first.networktables.NetworkTableInstance;

import imgui.ImGui;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public final class NetworkTablesConnection {
    public static final class Params {
        private final boolean isAddress;
        private final String host;
        private final int portOrTeam;

        public Params(int team) {
            isAddress = false;
            host = null;
            this.portOrTeam = team;
        }

        public Params(String host, int portOrTeam) {
            isAddress = true;
            this.host = host;
            this.portOrTeam = portOrTeam;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Params params = (Params) o;
            return isAddress == params.isAddress
                    && portOrTeam == params.portOrTeam
                    && Objects.equals(host, params.host);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isAddress, host, portOrTeam);
        }
    }

    public enum Status {
        IDLE("Not Connected", 1, 0, 0),
        CONNECTED("Connected", 0, 1, 0),
        CLOSING("Switching", 1, 0.5f, 0);

        private final String friendlyName;
        private final int color;

        Status(String friendlyName, float r, float g, float b) {
            this.friendlyName = friendlyName;
            this.color = ImGui.colorConvertFloat4ToU32(r, g, b, 1);
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        public int getColor() {
            return color;
        }
    }

    private static final String CLIENT_ID = "ShuffleLog";

    private final ExecutorService threadPool;
    private NetworkTableInstance instance;
    private Future<?> stopFuture;
    private Boolean isNt4;
    private Params params;

    private NetworkTableRepr rootTable;
    private final AtomicInteger activeInstances;

    public NetworkTablesConnection(ExecutorService threadPool) {
        this.threadPool = threadPool;
        instance = null;
        stopFuture = null;
        isNt4 = null;
        params = null;

        activeInstances = new AtomicInteger(0);
    }

    public void setServerParams(boolean isNt4, Params params) {
        // Start a new client if there is not one currently
        if (instance == null) {
            instance = NetworkTableInstance.create();
            activeInstances.incrementAndGet();

            if (isNt4) instance.startClient4(CLIENT_ID);
            else instance.startClient3(CLIENT_ID);

            this.isNt4 = isNt4;

            if (params.isAddress) instance.setServer(params.host, params.portOrTeam);
            else instance.setServerTeam(params.portOrTeam);

            rootTable = new NetworkTableRepr(instance.getTable("/"));
            this.params = params;
        }

        // If the current client already satisfies the desired parameters,
        // it doesn't need to restart
        if (this.isNt4 == isNt4 && params.equals(this.params)) return;

        // Wait for the stop future to finish so we don't create
        // too many instances (there is a maximum of 16)
        if (stopFuture != null && !stopFuture.isDone()) return;

        // Dispose of cached entries if they exist
        if (rootTable != null) rootTable.close();
        rootTable = null;

        // Save a reference to the current instance and clear instance variable so
        // the instance can't be used after closing
        NetworkTableInstance savedInstance = instance;
        instance = null;

        // Stop on other thread because stopping the client can take around
        // 10 seconds sometimes, and we don't want to freeze the GUI
        // We need to completely restart the NT instance because NT does not clear
        // local entries when switching servers
        stopFuture =
                threadPool.submit(
                        () -> {
                            savedInstance.setServer(new String[0], 0);
                            savedInstance.stopClient();
                            savedInstance.stopLocal();
                            savedInstance.close();
                            activeInstances.decrementAndGet();
                        });
    }

    public Status getStatus() {
        if (stopFuture != null && !stopFuture.isDone()) return Status.CLOSING;

        return (instance != null && instance.isConnected()) ? Status.CONNECTED : Status.IDLE;
    }

    // Can be null if client is not running
    public NetworkTableRepr getRootTable() {
        return rootTable;
    }

    public int getActiveInstances() {
        return activeInstances.get();
    }
}
