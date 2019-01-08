package org.artb.chat.server;

import org.artb.chat.server.core.ChatServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRunner.class);

    private static final int PORT = 8999;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        ChatServer server = new ChatServer(HOST, PORT);
        try {
            server.start();
        } catch (Exception e) {
            LOGGER.error("Server error", e);
        }
    }
}
