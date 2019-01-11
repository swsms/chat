package org.artb.chat.common.connection;

import java.io.IOException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BufferedConnection extends IdentifiableConnection {

    private final Connection connection;
    private final Queue<String> dataBuffer = new ConcurrentLinkedQueue<>();

    public BufferedConnection(UUID id, Connection connection) {
        super(id);
        this.connection = connection;
    }

    public void sendPendingData() throws IOException {
        String next;
        while ((next = dataBuffer.poll()) != null) {
            connection.send(next);
        }
    }

    @Override
    public void send(String msg) throws IOException {
        connection.send(msg);
    }

    @Override
    public String take() throws IOException {
        return connection.take();
    }

    public void notification() {
        connection.notification();
    }

    @Override
    public boolean connect() throws IOException {
        return connection.connect();
    }

    @Override
    public void close() throws IOException {
        connection.close();
        dataBuffer.clear();
    }

    public void addToBuffer(String data) {
        dataBuffer.add(data);
    }
}
