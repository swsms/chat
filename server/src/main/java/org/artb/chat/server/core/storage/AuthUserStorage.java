package org.artb.chat.server.core.storage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// TODO work on it
public class AuthUserStorage {

    private ConcurrentHashMap<UUID, String> authUsers = new ConcurrentHashMap<>();

    public void saveUser(UUID clientId, String name) {
        authUsers.putIfAbsent(clientId, name);
    }

    public String getUserName(UUID clientId) {
        return authUsers.get(clientId);
    }

    public boolean containsUserName(String name) {
        return authUsers.containsValue(name);
    }

    public boolean authenticated(UUID clientId) {
        return authUsers.containsKey(clientId);
    }

    public String removeUser(UUID clientId) {
        return authUsers.remove(clientId);
    }

    public Collection<String> getUsers() {
        return authUsers.values();
    }
}
