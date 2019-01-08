package org.artb.chat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.OP_CONNECT;

public class ClientRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRunner.class);

    private static final int PORT = 10521;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        try {
            SocketChannel socket = SocketChannel.open();
            socket.configureBlocking(false);

            Selector selector = Selector.open();

            socket.register(selector, OP_CONNECT);
            socket.connect(new InetSocketAddress(HOST, PORT));
        } catch (Exception e) {
            LOGGER.info("Cannot establish connection: {}", e.getMessage());
        }
    }
}