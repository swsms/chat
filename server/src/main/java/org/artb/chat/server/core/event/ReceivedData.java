package org.artb.chat.server.core.event;

import org.artb.chat.common.connection.BufferedConnection;

import java.util.UUID;

public class ReceivedData {

    private final UUID clientId;
    private final String rawData;
    private final BufferedConnection connection;

    public ReceivedData(UUID clientId, String rawData, BufferedConnection connection) {
        this.clientId = clientId;
        this.rawData = rawData;
        this.connection = connection;
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getRawData() {
        return rawData;
    }

    public BufferedConnection getConnection() {
        return connection;
    }

    @Override
    public String toString() {
        return "ReceivedData{" +
                "clientId=" + clientId +
                ", rawData='" + rawData + '\'' +
                '}';
    }
}
