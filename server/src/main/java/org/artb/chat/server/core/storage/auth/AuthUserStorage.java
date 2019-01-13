package org.artb.chat.server.core.storage.auth;

import java.util.Collection;
import java.util.UUID;

public interface AuthUserStorage {

    String getUserName(UUID clientId);

    boolean containsUserName(String name);

    boolean authenticated(UUID clientId);

    String removeUser(UUID clientId);

    Collection<String> getUsers();

    void upsertUserName(UUID clientId, String newName) throws InvalidNameException;
}
