package org.artb.chat.common;

import java.util.UUID;

public abstract class Identifiable {

    private final UUID id;

    public Identifiable(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
