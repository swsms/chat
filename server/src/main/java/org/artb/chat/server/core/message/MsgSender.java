package org.artb.chat.server.core.message;

import org.artb.chat.common.message.Message;

import java.util.UUID;

public interface MsgSender {

    /**
     * Broadcast sending to all authenticated users
     */
    void sendAll(Message msg);

    /**
     * Send a message to a user
     */
    void sendOne(UUID targetId, Message msg);
}
