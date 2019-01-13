package org.artb.chat.server.core;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.server.core.event.MessageArrivedEvent;
import org.artb.chat.server.core.event.ReceivedData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ServerProcessor {
    protected final String host;
    protected final int port;

    protected final AtomicBoolean runningFlag = new AtomicBoolean();
    protected final Map<UUID, BufferedConnection> connections = new ConcurrentHashMap<>();
    protected final BlockingQueue<ReceivedData> receivedDataQueue = new LinkedBlockingQueue<>();

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

    public BlockingQueue<ReceivedData> getReceivedDataQueue() {
        return receivedDataQueue;
    }
}
