package org.artb.chat.client.core.processor;

import org.artb.chat.common.Lifecycle;
import java.util.function.Consumer;

public abstract class ClientProcessor implements Lifecycle {
    protected final String serverHost;
    protected final int serverPort;

    protected volatile boolean running;

    /**
     * Potentially the fields below can be set after the processor starts in a separated thread
     */
    protected Consumer<String> receivedDataListener = (data) -> { };
    protected DisconnectHandler handler = DisconnectHandler::nothing;

    protected ClientProcessor(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void setReceivedDataListener(Consumer<String> receivedDataListener) {
        this.receivedDataListener = receivedDataListener;
    }

    public void setDisconnectHandler(DisconnectHandler handler) {
        this.handler = handler;
    }

    public abstract void acceptData(String data);
}
