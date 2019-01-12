package org.artb.chat.common.connection;

import java.util.UUID;

public abstract class IdentifiableConnection implements Connection {

    private final UUID id;

    public IdentifiableConnection(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
