package org.artb.chat.server.core.storage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuthUserStorage {

    private ConcurrentHashMap<String, UUID> authenticatedUsers = new ConcurrentHashMap<>();

    public boolean put(UUID clientId, String name) {
        UUID oldId = authenticatedUsers.putIfAbsent(name, clientId);
        return oldId == null;
    }

    public boolean exists(String name) {
        return authenticatedUsers.containsKey(name);
    }
}
