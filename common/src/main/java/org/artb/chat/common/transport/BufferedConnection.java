package org.artb.chat.common.transport;

import org.artb.chat.common.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Just wrap a single connection with a buffer
 */
public class BufferedConnection implements Connection, HasBuffer {

    private final Connection connection;
    private final Queue<String> dataBuffer = new ConcurrentLinkedQueue<>();

    public BufferedConnection(Connection connection) {
        this.connection = connection;
    }

    public void flush() throws IOException {
        String next;
        List<String> messages = new ArrayList<>();
        while ((next = dataBuffer.poll()) != null) {
            messages.add(next);
        }
        connection.send(Utils.createBatch(messages));
    }

    @Override
    public void putInBuffer(String data) {
        dataBuffer.add(data);
    }

    @Override
    public void send(String data) throws IOException {
        connection.send(data);
    }

    @Override
    public String take() throws IOException {
        return connection.take();
    }

    @Override
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

    @Override
    public UUID getId() {
        return connection.getId();
    }
}
