package org.artb.chat.server.core.event;

import java.util.UUID;

public abstract class Event {
    protected final UUID clientId;

    protected Event(UUID clientId) {
        this.clientId = clientId;
    }

    public UUID getClientId() {
        return clientId;
    }
}
