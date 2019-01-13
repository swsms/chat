package org.artb.chat.server.core.message;

import org.artb.chat.common.Utils;
import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BasicMsgSender implements MsgSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicMsgSender.class);

    private final AuthUserStorage userStorage;
    private final Map<UUID, BufferedConnection> connections;

    public BasicMsgSender(AuthUserStorage userStorage,
                          Map<UUID, BufferedConnection> connections) {
        this.userStorage = userStorage;
        this.connections = connections;
    }

    @Override
    public void sendBroadcast(Message msg) {
        try {
            String jsonMsg = Utils.serialize(msg);
            connections.forEach((id, connection) -> {
                if (userStorage.authenticated(id)) {
                    connection.addToBuffer(jsonMsg);
                    connection.notification();
                }
            });
        } catch (IOException e) {
            LOGGER.info("Cannot send message: {}", msg, e);
        }
    }

    @Override
    public void send(UUID id, Message msg) {
        send(id, Collections.singletonList(msg));
    }

    @Override
    public void send(UUID targetId, List<Message> msgList) {
        BufferedConnection connection = connections.get(targetId);
        if (connection == null) {
            LOGGER.warn("No connection for id found: {}", targetId);
        } else {
            msgList.forEach(msg -> {
                try {
                    String jsonMsg = Utils.serialize(msg);
                    connection.addToBuffer(jsonMsg);
                } catch (IOException e) {
                    LOGGER.error("Cannot send message: {} to {}", msg, targetId);
                }
            });
            connection.notification();
        }
    }
}
