package org.artb.chat.server.core.message.consumer;

import org.artb.chat.common.connection.BufferedConnection;

import java.util.List;

public class ConnectionDataConsumer implements DataConsumer {

    private final BufferedConnection connection;

    public ConnectionDataConsumer(BufferedConnection connection) {
        this.connection = connection;
    }

    @Override
    public void consume(String data) {
        connection.addToBuffer(data);
        connection.notification();
    }

    @Override
    public void consume(List<String> dataList) {
        dataList.forEach(connection::addToBuffer);
        connection.notification();

    }
}
