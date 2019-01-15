package org.artb.chat.server.core.storage.auth;

import java.util.Map;
import java.util.UUID;

public interface AuthUserStorage {

    String getUserName(UUID clientId);

    boolean containsUserName(String name);

    boolean authenticated(UUID clientId);

    String removeUser(UUID clientId);

    Map<UUID, String> getUsers();

    /**
     * Update or insert a user name.
     * The name of a user should be always unique
     */
    void upsertUserName(UUID clientId, String newName) throws InvalidNameException;
}
