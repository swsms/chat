package org.artb.chat.server.core.message;

import org.artb.chat.common.Utils;
import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class BasicMsgSender implements MsgSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicMsgSender.class);

    private final Map<UUID, BufferedConnection> connections;

    public BasicMsgSender(Map<UUID, BufferedConnection> connections) {
        this.connections = connections;
    }

    @Override
    public void sendAll(Message msg) {
        try {
            String jsonMsg = Utils.serialize(msg);
            connections.forEach((id, connection) -> {
                if (connection.isAuthenticated()) {
                    connection.addToBuffer(jsonMsg);
                    connection.notification();
                }
            });
        } catch (IOException e) {
            LOGGER.info("Cannot send message: {}", msg, e);
        }
    }

    @Override
    public void sendOne(UUID id, Message msg) {
        try {
            String jsonMsg = Utils.serialize(msg);
            BufferedConnection connection = connections.get(id);
            if (connection == null) {
                LOGGER.warn("No connection, cannot send message {} to {}", msg, id);
            } else {
                connection.addToBuffer(jsonMsg);
                connection.notification();
            }
        } catch (IOException e) {
            LOGGER.info("Cannot send message: {} to {}", msg, id, e);
        }
    }
}
