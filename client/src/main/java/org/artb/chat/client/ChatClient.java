package org.artb.chat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRunner.class);

    private final String serverHost;
    private final int serverPort;

    public ChatClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void start() {

    }
}
