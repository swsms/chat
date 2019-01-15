package org.artb.chat.server.core.message;

import org.artb.chat.common.message.Message;

import java.util.List;
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
     * Send a message to user
     */
    void sendPersonal(UUID targetId, Message msg);

    /**
     * Send several messages to user
     */
    void sendPersonal(UUID targetId, List<Message> msgList);
}
