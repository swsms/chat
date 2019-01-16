package org.artb.chat.client.core.transport;

import org.artb.chat.common.Lifecycle;
import java.util.function.Consumer;

/**
 * Represents the transport layer for the client. To add a new transport you need extend it.
 */
public abstract class ClientProcessor implements Lifecycle {
    protected final String serverHost;
    protected final int serverPort;

    protected volatile boolean running;

    protected final Consumer<String> receivedDataListener ;
    protected final DisconnectHandler handler;

    protected ClientProcessor(String serverHost, int serverPort,
                              Consumer<String> receivedDataListener,
                              DisconnectHandler handler) {

        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.receivedDataListener = receivedDataListener;
        this.handler = handler;
    }

    public abstract void acceptData(String data);

    public boolean isRunning() {
        return running;
    }
}
