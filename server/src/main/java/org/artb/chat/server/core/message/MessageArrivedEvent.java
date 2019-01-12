package org.artb.chat.server.core.message;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;

import java.util.UUID;

public class MessageArrivedEvent {

    private final UUID clientId;
    private final Message message;
    private final BufferedConnection connection;

    public MessageArrivedEvent(UUID clientId, Message msg, BufferedConnection connection) {
        this.clientId = clientId;
        this.message = msg;
        this.connection = connection;
    }

    public Message getMessage() {
        return message;
    }

    public BufferedConnection getConnection() {
        return connection;
    }

    public UUID getClientId() {
        return clientId;
    }

    @Override
    public String toString() {
        return "MessageArrivedEvent{" +
                "clientId=" + clientId +
                ", message=" + message +
                '}';
    }
}
