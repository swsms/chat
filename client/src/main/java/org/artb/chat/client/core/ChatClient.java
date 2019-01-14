package org.artb.chat.client.core;

import org.artb.chat.client.ui.UIConsoleDisplay;
import org.artb.chat.client.ui.UIDisplay;
import org.artb.chat.common.ChatComponent;
import org.artb.chat.common.Utils;
import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.connection.Connection;
import org.artb.chat.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ChatClient implements ChatComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);

    protected final String serverHost;
    protected final int serverPort;

    protected BufferedConnection connection;
    protected final UIDisplay display = new UIConsoleDisplay();

    protected final AtomicBoolean running = new AtomicBoolean();
    private MessageReader reader;
    private final InputStream input;

    protected ChatClient(String serverHost, int serverPort) {
        this(serverHost, serverPort, System.in);
    }

    protected ChatClient(String serverHost, int serverPort, InputStream input) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.input = input;
    }

    protected abstract BufferedConnection configureConnection() throws ClientException;

    protected abstract void doMainLogic() throws ClientException;

    private void send(Message msg) {
        try {
            String data = Utils.serialize(msg);
            connection.putInBuffer(data);
            connection.notification();
        } catch (IOException e) {
            LOGGER.error("Cannot serialize msg: {}", msg, e);
        }
    }

    public void start() {
        running.set(true);

        try {
            this.connection = configureConnection();

            this.reader = new MessageReader(this::send, input, running);
            Thread asyncReader = new Thread(reader);
            asyncReader.start();

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
            display.print("Successfully disconnected. Press any key to exit the client program.");
        } catch (IOException e) {
            LOGGER.error("Cannot close socket", e);
        }
    }
}
