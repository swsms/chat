package org.artb.chat.server.core.event;

import org.artb.chat.common.connection.BufferedConnection;

import java.util.UUID;

public abstract class Event {
    protected final UUID clientId;
    protected final BufferedConnection connection;

    protected Event(UUID clientId, BufferedConnection connection) {
        this.clientId = clientId;
        this.connection = connection;
    }

    public UUID getClientId() {
        return clientId;
    }

    public BufferedConnection getConnection() {
        return connection;
    }
}
