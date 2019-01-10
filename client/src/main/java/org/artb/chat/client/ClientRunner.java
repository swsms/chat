package org.artb.chat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRunner.class);

    private static final int PORT = 8999;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        ChatClient client = new ChatClient(HOST, PORT);
        client.start();
    }
}