package org.artb.chat.server.core.transport;

import org.artb.chat.common.Lifecycle;
import org.artb.chat.common.transport.Connection;
import org.artb.chat.server.core.ReceivedData;
import org.artb.chat.server.core.event.ConnectionEvent;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents the transport layer for the server. To add a new transport you need extend it.
 */
public abstract class ServerProcessor implements Lifecycle {
    protected final String host;
    protected final int port;

    protected volatile boolean running;

    protected final Consumer<ConnectionEvent> connectionEventListener;
    protected final Consumer<ReceivedData> receivedDataListener;

    public ServerProcessor(String host, int port,
                           Consumer<ConnectionEvent> connectionEventListener,
                           Consumer<ReceivedData> receivedDataListener) {
        this.host = host;
        this.port = port;
        this.connectionEventListener = connectionEventListener;
        this.receivedDataListener = receivedDataListener;
    }

    public abstract void acceptData(UUID clientId, String data);

    public abstract void disconnect(UUID id);
}
