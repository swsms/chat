package org.artb.chat.server.core.message;

import org.artb.chat.common.Utils;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.transport.tcpnio.DataReceiver;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.artb.chat.server.core.storage.history.HistoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class BasicMessageSender implements MessageSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicMessageSender.class);

    private final AuthUserStorage userStorage;
    private final HistoryStorage historyStorage;
    private final DataReceiver consumer;

    public BasicMessageSender(AuthUserStorage userStorage,
                              DataReceiver consumer,
                              HistoryStorage historyStorage) {
        this.userStorage = userStorage;
        this.historyStorage = historyStorage;
        this.consumer = consumer;
    }

    @Override
    public void sendBroadcast(Message msg) {
        userStorage.getUsers().forEach((id, name) -> sendPersonal(id, msg));
        historyStorage.add(msg);
    }

    @Override
    public void sendPersonal(UUID userId, Message msg) {
        try {
            String jsonMsg = Utils.serialize(msg);
            consumer.accept(userId, jsonMsg);
        } catch (IOException e) {
            LOGGER.error("Cannot send message: {} to {}", msg, userId, e);
        }
    }
}
