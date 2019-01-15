package org.artb.chat.server.core.event;

import java.util.UUID;

public class ConnectionEvent extends Event {
    private final ConnectionEventType type;

    public ConnectionEvent(UUID clientId, ConnectionEventType type) {
        super(clientId);
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
