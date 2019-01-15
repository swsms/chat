package org.artb.chat.server.core.message;

import org.artb.chat.common.message.Message;

import java.util.UUID;

/**
 * Represents a protocol-independent sender
 */
public interface MessageSender {

    /**
     * Send message to all authenticated users
     */
    void sendBroadcast(Message msg);

    /**
     * Send a message to the specified user
     */
    void sendPersonal(UUID userId, Message msg);
}
