package org.artb.chat.server.core.storage.auth;

import org.artb.chat.common.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryAuthUserStorage implements AuthUserStorage {

    private ConcurrentHashMap<UUID, String> authUsers = new ConcurrentHashMap<>();
    private final Lock locker = new ReentrantLock();

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

    public Map<UUID, String> getUsers() {
        return Collections.unmodifiableMap(authUsers);
    }

    public void upsertUserName(UUID clientId, String newName) throws InvalidNameException {
        if (Utils.isBlank(newName)) {
            throw new InvalidNameException("The name should not be empty, try another one.");
        }

        locker.lock();
        try {
            if (containsUserName(newName)) {
                throw new InvalidNameException("The name " + newName + " is already in use, try another one.");
            }
            authUsers.put(clientId, newName);
        } finally {
            locker.unlock();
        }
    }
}
