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
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ChatClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);

    protected final String serverHost;
    protected final int serverPort;

    protected Connection connection;
    protected final UIDisplay display = new UIConsoleDisplay();

    protected final AtomicBoolean running = new AtomicBoolean();
    protected final Queue<Message> messages = new ConcurrentLinkedQueue<>();
    private final MsgReader msgReader = new MsgReader(this::enqueueMessage, running);

    protected ChatClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    private void enqueueMessage(Message msg) {
        messages.add(msg);
        connection.notification();
    }

    protected abstract Connection configureConnection() throws ClientException;

    protected abstract void doMainLogic() throws ClientException;

    public void start() {
        running.set(true);

        Thread asyncMsgReader = new Thread(msgReader);
        asyncMsgReader.start();

        try {
            this.connection = configureConnection();
            doMainLogic();
        } catch (ClientException e) {
            LOGGER.error("Cannot connect to {}:{}", serverHost, serverPort, e);
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            running.set(false);
            if (connection != null) {
                connection.close();
            }
            LOGGER.info("Client successfully stopped");
        } catch (IOException e) {
            LOGGER.error("Cannot close socket", e);
        }
    }
}
