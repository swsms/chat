package org.artb.chat.server.core.storage.auth;

import org.artb.chat.common.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AuthUserStorage {

    private ConcurrentHashMap<UUID, String> authUsers = new ConcurrentHashMap<>();
    private final Lock locker = new ReentrantLock();

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

    // TODO move similar logic there
    public void renameUser(UUID clientId, String newName) throws InvalidNameException {
        if (Utils.isBlank(newName)) {
            throw new InvalidNameException("The name should not be empty.");
        }

        locker.lock();
        try {
            if (containsUserName(newName)) {
                throw new InvalidNameException("The name " + newName + " is already in use, try another one.");
            }
            String resultName = authUsers.computeIfPresent(clientId, (id, name) -> newName);
        } finally {
            locker.unlock();
        }
    }
}
