package org.artb.chat.server.core.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.artb.chat.common.Utils;
import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.message.BasicMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConnectionDataConsumer implements DataConsumer<String> {
    private final Map<UUID, BufferedConnection> connections;

    public ConnectionDataConsumer(Map<UUID, BufferedConnection> connections) {
        this.connections = connections;
    }

    @Override
    public void consume(UUID userId, String data) {
        consume(userId, Collections.singletonList(data));
    }

    @Override
    public void consume(UUID userId, List<String> dataList) {
        BufferedConnection connection = connections.get(userId);
        dataList.forEach((dataItem) -> {
            connection.putInBuffer(dataItem);
            connection.notification();
        });
    }
}
