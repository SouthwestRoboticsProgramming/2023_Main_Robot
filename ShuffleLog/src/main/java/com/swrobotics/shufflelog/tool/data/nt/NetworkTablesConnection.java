package com.swrobotics.shufflelog.tool.data.nt;

import edu.wpi.first.networktables.NetworkTableInstance;

public final class NetworkTablesConnection {
    private static final String CLIENT_ID = "ShuffleLog";

    public static NetworkTablesConnection fromTeamNumber(int teamNumber, boolean isNt4) {
        NetworkTableInstance instance = NetworkTableInstance.create();
        if (isNt4)
            instance.startClient4(CLIENT_ID);
        else
            instance.startClient3(CLIENT_ID);

        instance.setServerTeam(teamNumber);
        instance.startDSClient();

        return new NetworkTablesConnection(instance);
    }

    public static NetworkTablesConnection fromAddress(String host, int port, boolean isNt4) {
        NetworkTableInstance instance = NetworkTableInstance.create();
        if (isNt4)
            instance.startClient4(CLIENT_ID);
        else
            instance.startClient3(CLIENT_ID);

        instance.setServer(host, port);

        return new NetworkTablesConnection(instance);
    }

    private final NetworkTableInstance instance;

    private NetworkTablesConnection(NetworkTableInstance instance) {
        this.instance = instance;
    }

    public boolean isConnected() {
        return instance.isConnected();
    }

    public void close() {
        instance.stopClient();
        instance.stopDSClient();
        instance.close();
    }
}
