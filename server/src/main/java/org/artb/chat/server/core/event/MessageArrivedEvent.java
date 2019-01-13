package org.artb.chat.server.core.event;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;

import java.util.UUID;

public class MessageArrivedEvent extends Event {
    private final Message message;

    public MessageArrivedEvent(UUID clientId, Message msg, BufferedConnection connection) {
        super(clientId, connection);
        this.message = msg;
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "clientId='" + clientId + '\'' +
                ", message=" + message +
                '}';
    }
}
