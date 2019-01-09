package org.artb.chat.server.core.connection;

import java.util.UUID;

public class Session {

    private final UUID clientId;

    public Session(UUID clientId) {
        this.clientId = clientId;
    }

    public UUID getClientId() {
        return clientId;
    }
}
