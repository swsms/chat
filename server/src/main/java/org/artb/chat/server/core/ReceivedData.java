package org.artb.chat.server.core;

import java.util.UUID;

public class ReceivedData {

    private final UUID clientId;
    private final String rawData;

    public ReceivedData(UUID clientId, String rawData) {
        this.clientId = clientId;
        this.rawData = rawData;
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getRawData() {
        return rawData;
    }

    @Override
    public String toString() {
        return "ReceivedData{" +
                "clientId=" + clientId +
                ", rawData='" + rawData + '\'' +
                '}';
    }
}
