package org.artb.chat.server.core.storage.auth;

import java.util.Map;
import java.util.UUID;

public interface AuthUserStorage {

    String getUserName(UUID clientId);

    boolean containsUserName(String name);

    boolean authenticated(UUID clientId);

    String removeUser(UUID clientId);

    Map<UUID, String> getUsers();

    void upsertUserName(UUID clientId, String newName) throws InvalidNameException;
}
