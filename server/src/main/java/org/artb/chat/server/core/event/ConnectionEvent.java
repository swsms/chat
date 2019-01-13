package org.artb.chat.server.core.event;

import org.artb.chat.common.connection.BufferedConnection;

import java.util.UUID;

public class ConnectionEvent extends Event {
    private final ConnectionEventType type;

    public ConnectionEvent(UUID clientId, BufferedConnection connection, ConnectionEventType type) {
        super(clientId, connection);
        this.type = type;
    }

    public ConnectionEventType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "clientId='" + clientId + '\'' +
                ", type=" + type +
                '}';
    }
}
