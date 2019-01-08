package org.artb.chat.server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

public class ChatServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final String host;
    private final int port;

    private Selector selector;
    private ServerSocketChannel serverSocket;

    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    private Map<UUID, SocketChannel> connections = new ConcurrentHashMap<>();

    private LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();

    private volatile boolean running = false;

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();

        serverSocket.socket().setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(host, port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, OP_ACCEPT);

        LOGGER.info("Server started on {}:{}", host, port);
        running = true;

        Thread connectionProcessor = new Thread(() -> {
            LOGGER.info("Connection processor started");
            while (running) {
                try {
                    String message = messages.take();
                    LOGGER.info("Message to send: {}", message);
                    broadcast(message);
                } catch (InterruptedException e) {
                    LOGGER.error("", e);
                }
            }
        });

        connectionProcessor.start();

        while (running) {
            int numKeys = selector.select();

            if (numKeys > 0) {
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        register(selector);
                    } else if (key.isReadable()) {
                        read(key);
                    }
                }
            }
        }
    }

    public void stop() {
        try {
            serverSocket.close();
            running = false;
        } catch (IOException e) {
            LOGGER.error("Cannot close server socket: ", e.getMessage());
        }
    }

    private void register(Selector selector) {
        try {
            SocketChannel client = serverSocket.accept();
            client.configureBlocking(false);
            client.register(selector, OP_READ);

            String remoteAddress = Objects.toString(client.getRemoteAddress());

            UUID clientId = UUID.randomUUID();
            connections.putIfAbsent(clientId, client);

            LOGGER.info("New client from {} registered with it ", remoteAddress, clientId);
        } catch (IOException e) {
            LOGGER.error("Can't register new client", e);
        }
    }

    private void read(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();

        try {
            client.read(buffer);
            if (client.read(buffer) < 0) {
                // TODO disconnect
            }
        } catch (IOException e) {
            // TODO disconnect
            return;
        }

        String message = new String(
                extractDataFromBuffer(buffer),
                StandardCharsets.UTF_8);

        messages.add(message);
    }

    private void broadcast(String message) {
        connections.forEach((id, client) -> {
            try {
                ByteBuffer buf = ByteBuffer.wrap(message.getBytes());
                client.write(buf);
                buffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private byte[] extractDataFromBuffer(ByteBuffer buffer) {
        buffer.flip();
        byte[] ret = new byte[buffer.limit()];
        buffer.get(ret);
        buffer.clear();
        return ret;
    }
}
