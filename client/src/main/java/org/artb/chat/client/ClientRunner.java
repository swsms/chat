package org.artb.chat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import static java.nio.channels.SelectionKey.OP_CONNECT;

public class ClientRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRunner.class);

    private static final int PORT = 8999;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        try {
            SocketChannel socket = SocketChannel.open(new InetSocketAddress(HOST, PORT));
            socket.configureBlocking(false);

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            Scanner scanner = new Scanner(System.in);

            String msg = scanner.nextLine();

            buffer = ByteBuffer.wrap(msg.getBytes());

            socket.write(buffer);
            buffer.clear();

            socket.read(buffer);
            String response = new String(buffer.array()).trim();
            buffer.clear();

            LOGGER.info("Response: {}", response);

        } catch (Exception e) {
            LOGGER.error("Cannot establish connection", e);
        }
    }
}