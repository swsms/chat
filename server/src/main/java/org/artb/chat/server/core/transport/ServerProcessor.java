package org.artb.chat.server.core.transport;

import org.artb.chat.common.Lifecycle;
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

    /* (something) -> { } is a function for default listeners to avoid nulls */
    protected Consumer<ConnectionEvent> connectionEventListener = (event) -> { };
    protected Consumer<ReceivedData> receivedDataListener = (data) -> { };

    public ServerProcessor(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setReceivedDataListener(Consumer<ReceivedData> receivedDataListener) {
        this.receivedDataListener = receivedDataListener;
    }

    public void setConnectionEventListener(Consumer<ConnectionEvent> listener) {
        this.connectionEventListener = listener;
    }

    public abstract void acceptData(UUID clientId, String data);

    public abstract void disconnect(UUID id);
}
