package org.artb.chat.server.core.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserInfoStorage {

    private Map<UUID, String> authenticatedUsers = new ConcurrentHashMap<>();

    public void put(UUID clientId, String name) {
        authenticatedUsers.putIfAbsent(clientId, name);
    }

    public boolean exists(String name) {
        return authenticatedUsers.containsValue(name);
    }

    public boolean isAuthenticated(UUID clientId) {
        return authenticatedUsers.containsKey(clientId);
    }

    public void updateName(UUID clientId, String newName) {
        authenticatedUsers.computeIfPresent(clientId, (id, name) -> newName);
    }
}
