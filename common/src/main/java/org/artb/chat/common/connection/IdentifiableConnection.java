package org.artb.chat.common.connection;

import org.artb.chat.common.Utils;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class IdentifiableConnection implements Connection {

    private final UUID id;
    private final LocalDateTime created = LocalDateTime.now();
    private volatile String userName;

    public IdentifiableConnection(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isAuthenticated() {
        return Utils.nonBlank(userName);
    }
}
