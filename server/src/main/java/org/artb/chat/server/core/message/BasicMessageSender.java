package org.artb.chat.server.core.message;

import org.artb.chat.common.Utils;
import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.artb.chat.server.core.storage.history.HistoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BasicMessageSender implements MessageSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicMessageSender.class);

    private final AuthUserStorage userStorage;
    private final Map<UUID, BufferedConnection> connections;
    private final HistoryStorage historyStorage;

    public BasicMessageSender(AuthUserStorage userStorage,
                              Map<UUID, BufferedConnection> connections,
                              HistoryStorage historyStorage) {
        this.userStorage = userStorage;
        this.connections = connections;
        this.historyStorage = historyStorage;
    }

    // TODO use another send
    @Override
    public void sendBroadcast(Message msg) {
        try {
            String jsonMsg = Utils.serialize(msg);
            userStorage.getUsers().forEach((id, name) -> {
                BufferedConnection connection = connections.get(id);
                connection.addToBuffer(jsonMsg);
                connection.notification();
            });
            historyStorage.add(msg);
        } catch (IOException e) {
            LOGGER.info("Cannot send message: {}", msg, e);
        }
    }

    @Override
    public void sendPersonal(UUID id, Message msg) {
        sendPersonal(id, Collections.singletonList(msg));
    }

    @Override
    public void sendPersonal(UUID targetId, List<Message> msgList) {
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
