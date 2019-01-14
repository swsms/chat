package org.artb.chat.server.core;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.server.core.event.ConnectionEvent;
import org.artb.chat.server.core.event.ReceivedData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class ServerProcessor {
    protected final String host;
    protected final int port;

    protected final AtomicBoolean runningFlag = new AtomicBoolean();
    protected final Map<UUID, BufferedConnection> connections = new ConcurrentHashMap<>();

    /* (something) -> { } is a function for default listeners to avoid nulls */
    protected Consumer<ConnectionEvent> connectionEventListener = (event) -> { };
    protected Consumer<ReceivedData> receivedDataListener = (data) -> { };

    public ServerProcessor(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public abstract void start();

    public abstract void stop();

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Map<UUID, BufferedConnection> getConnections() {
        return connections;
    }

    public AtomicBoolean getRunningFlag() {
        return runningFlag;
    }

    public void setReceivedDataListener(Consumer<ReceivedData> receivedDataListener) {
        this.receivedDataListener = receivedDataListener;
    }

    public void setConnectionEventListener(Consumer<ConnectionEvent> listener) {
        this.connectionEventListener = listener;
    }
}
