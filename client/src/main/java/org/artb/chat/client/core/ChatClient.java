package org.artb.chat.client.core;

import org.artb.chat.client.ui.UIConsoleDisplay;
import org.artb.chat.client.ui.UIDisplay;
import org.artb.chat.common.connection.Connection;
import org.artb.chat.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class ChatClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);

    protected final String serverHost;
    protected final int serverPort;

    protected Connection connection;
    protected volatile boolean running = true;

    protected final UIDisplay display = new UIConsoleDisplay();

    protected final Queue<Message> messages = new ConcurrentLinkedQueue<>();
    private final AsyncMsgReader asyncMessageReader = new AsyncMsgReader(this::enqueueMessage);

    protected ChatClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    private void enqueueMessage(Message msg) {
        messages.add(msg);
        connection.notification();
    }

    protected abstract void configure() throws ClientException;

    protected abstract void doMainLogic() throws ClientException;

    public void start() {
        running = true;
        try {
            configure();

            asyncMessageReader.start();

            doMainLogic();
        } catch (ClientException e) {
            LOGGER.error("Cannot connect to {}:{}", serverHost, serverPort, e);
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            running = false;
            asyncMessageReader.stop();
            if (connection != null) {
                connection.close();
            }
            LOGGER.info("Client successfully stopped");
        } catch (IOException e) {
            LOGGER.error("Cannot close socket", e);
        }
    }
}
