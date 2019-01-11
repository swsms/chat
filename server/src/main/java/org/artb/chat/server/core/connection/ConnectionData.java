package org.artb.chat.server.core.connection;

import org.artb.chat.common.Utils;
import org.artb.chat.common.connection.Connection;
import org.artb.chat.common.message.Message;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionData {

    private final UUID clientId;
    private final Connection connection;
    private final Queue<String> dataBuffer = new ConcurrentLinkedQueue<>();

    private volatile String name;

    public ConnectionData(UUID clientId, Connection connection) {
        this.clientId = clientId;
        this.connection = connection;
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAuthenticated() {
        return Utils.nonBlank(name);
    }

    public Connection getConnection() {
        return connection;
    }

    public void addToBuffer(String data) {
        dataBuffer.add(data);
    }

    public String pollFromBuffer() {
        return dataBuffer.poll();
    }

    @Override
    public String toString() {
        return "ConnectionData{" +
                "clientId=" + clientId +
                ", name='" + name + '\'' +
                '}';
    }
}
