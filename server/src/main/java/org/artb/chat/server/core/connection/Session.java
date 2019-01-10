package org.artb.chat.server.core.connection;

import org.artb.chat.common.Utils;

import java.util.UUID;

public class Session {

    private final UUID clientId;
    private volatile String name;

    public Session(UUID clientId) {
        this.clientId = clientId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAuth() {
        return Utils.nonBlank(name);
    }

    @Override
    public String toString() {
        return "Session{" +
                "clientId=" + clientId +
                ", name='" + name + '\'' +
                '}';
    }
}
