package org.artb.chat.server.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.message.Utils;
import org.artb.chat.common.message.Utils;
import org.artb.chat.server.core.connection.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static org.artb.chat.server.core.Constants.*;
import static org.artb.chat.server.core.SendingTask.*;

public class ChatServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final String host;
    private final int port;

    private Selector selector;
    private ServerSocketChannel serverSocket;

    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    private Map<UUID, SocketChannel> connections = new ConcurrentHashMap<>();

    private BlockingQueue<SendingTask> sendingTasks = new LinkedBlockingQueue<>();

    private volatile boolean running = false;

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            running = true;

            configure();

            LOGGER.info("Server started on {}:{}", host, port);

            startAsyncTaskProcessing();

            while (running) {
                processKeys();
            }

        } catch (IOException e) {
            LOGGER.error("Cannot start server on {}:{}", host, port, e);
            running = false;
        }

        try {
            releaseSocket();
            LOGGER.info("Server loop has been successfully stopped");
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    private void configure() throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();

        serverSocket.bind(new InetSocketAddress(host, port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, OP_ACCEPT);
    }

    private void processKeys() throws IOException {
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
                    register(key);
                } else if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    private void releaseSocket() throws IOException {
        connections.forEach((id, connection) -> closeConnection(connection));
        serverSocket.close();
    }

    private void startAsyncTaskProcessing() {
        Thread connectionProcessor = new Thread(() -> {
            while (running) {
                try {
                    SendingTask task = sendingTasks.take();
                    LOGGER.info("Message to send: {}", task.getMessage());

                    String msgJson = Utils.serialize(task.getMessage());
                    switch (task.getMode()) {
                        case PERSONAL:
                            sendOne(task.getClientId(), msgJson);
                            break;
                        case BROADCAST:
                            sendBroadcast(msgJson);
                            break;
                    }
                } catch (Exception e) {
                    LOGGER.error("Cannot send message", e);
                }
            }
        });
        connectionProcessor.start();
    }

    public void stop() {
        running = false;
    }

    private void register(SelectionKey key) {
        UUID clientId = UUID.randomUUID();

        final SocketChannel client;
        try {
            client = ((ServerSocketChannel) key.channel()).accept();
            client.configureBlocking(false);
            client.register(selector, OP_READ, new Session(clientId));
            String remoteAddress = Objects.toString(client.getRemoteAddress());
            LOGGER.info("New client {} accepted from {}", clientId, remoteAddress);
        } catch (IOException e) {
            LOGGER.error("Cannot register new client with id {}", clientId, e);
            return;
        }

        connections.putIfAbsent(clientId, client);
        enqueue(newPersonalTask(REQUEST_NAME_MSG, clientId));
    }

    private void read(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();

        try {
            client.read(buffer);
            if (client.read(buffer) < 0) {
                closeConnection(client);
            }
        } catch (IOException e) {
            closeConnection(client);
            return;
        }

        Session session = (Session) key.attachment();

        try {
            Message msg = Utils.readMessage(buffer);
            LOGGER.info("{}", msg);
            if (session.isAuth()) {
                msg.setSender(session.getName()); // server knows the actual name of the client
                enqueue(newBroadcastTask(msg));
            } else {
                String userName = msg.getContent();
                if (Utils.isBlank(userName)) {
                    enqueue(newPersonalTask(REQUEST_NAME_MSG, session.getClientId()));
                } else if ("server".equalsIgnoreCase(userName)) {
                    enqueue(newPersonalTask(NAME_DECLINED_MSG, session.getClientId()));
                } else {
                    session.setName(userName);
                    enqueue(newPersonalTask(NAME_ACCEPTED_MSG, session.getClientId()));
                }
            }
        } catch (IOException e) {
            LOGGER.error("Incorrect message received", e);
            enqueue(newPersonalTask(INCORRECT_FORMAT_MSG, session.getClientId()));
        }
    }

    private void enqueue(SendingTask task) {
        sendingTasks.add(task);
    }

    private void sendBroadcast(String message) {
        connections.forEach((id, client) -> sendOne(id, message));
    }

    private void sendOne(UUID clientId, String message) {
        try {
            SocketChannel client = connections.get(clientId);
            if (client == null) {
                LOGGER.warn("Unknown connection with id: {}", clientId);
            } else {
                ByteBuffer buf = ByteBuffer.wrap(message.getBytes());
                client.write(buf);
            }
        } catch (IOException e) {
            LOGGER.error("Cannot send message to {}", clientId, e);
        }
    }

    private void closeConnection(SocketChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            LOGGER.error("Error when closing connection", e);
        }
    }
}
