package org.artb.chat.server.core.connection;

import org.artb.chat.common.Utils;
import org.artb.chat.common.connection.Connection;
import org.artb.chat.common.message.Message;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Session {

    private final UUID clientId;
    private final Connection connection;
    private final Queue<String> pendingData = new ConcurrentLinkedQueue<>();

    private volatile String name;

    public Session(UUID clientId, Connection connection) {
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

    public boolean isAuth() {
        return Utils.nonBlank(name);
    }

    public void addDataItem(String str) {
        pendingData.add(str);
    }

    public String getItemData() {
        return pendingData.poll();
    }

    public Queue<String> getPendingData() {
        return pendingData;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public String toString() {
        return "Session{" +
                "clientId=" + clientId +
                ", name='" + name + '\'' +
                '}';
    }
}
