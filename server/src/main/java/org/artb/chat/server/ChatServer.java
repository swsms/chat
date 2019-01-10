package org.artb.chat.server;

import org.artb.chat.common.connection.Connection;
import org.artb.chat.common.connection.TcpNioConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.Utils;
import org.artb.chat.server.core.task.AsyncTaskProcessor;
import org.artb.chat.server.core.task.SendingTask;
import org.artb.chat.server.core.connection.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static org.artb.chat.server.core.MsgConstants.*;
import static org.artb.chat.server.core.task.SendingTask.*;

public class ChatServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final String host;
    private final int port;

    private Map<UUID, Connection> connections = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    private final BlockingQueue<SendingTask> tasks = new LinkedBlockingQueue<>();
    private final AsyncTaskProcessor taskProcessor = new AsyncTaskProcessor(this, tasks);

    private Selector selector;
    private ServerSocketChannel serverSocket;

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        running = true;

        try {
            configure();
            LOGGER.info("Server started on {}:{}", host, port);
        } catch (IOException e) {
            LOGGER.error("Cannot start server on {}:{}", host, port, e);
            running = false;
        }

        taskProcessor.start();

        try {
            LOGGER.info("Starting process keys loop");
            while (running) {
                processKeys();
            }
        } catch (IOException e) {
            LOGGER.error("An error occurs while keys processing", e);
            running = false;
        }

        stop();

        LOGGER.info("The server has been stopped");
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

    private void stop() {
        running = false;
        taskProcessor.stop();
        try {
            connections.keySet().forEach(this::closeConnection);
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.error("Cannot close socket", e);
        }
    }

    private void register(SelectionKey key) {
        UUID clientId = UUID.randomUUID();

        final SocketChannel clientSocket;
        try {
            clientSocket = ((ServerSocketChannel) key.channel()).accept();
            clientSocket.configureBlocking(false);
            clientSocket.register(selector, OP_READ, new Session(clientId));
            String remoteAddress = Objects.toString(clientSocket.getRemoteAddress());
            LOGGER.info("New client {} accepted from {}", clientId, remoteAddress);
        } catch (IOException e) {
            LOGGER.error("Cannot register new client with id {}", clientId, e);
            return;
        }

        Connection connection = new TcpNioConnection(selector, clientSocket);
        connections.putIfAbsent(clientId, connection);

        enqueue(newPersonalTask(REQUEST_NAME_MSG, clientId));
    }

    private void read(SelectionKey key) {
        Session session = (Session) key.attachment();
        Connection connection = connections.get(session.getClientId());
        if (connection == null) {
            LOGGER.error("Unknown connection with id: {}", session.getClientId());
            return;
        }

        try {
            Message msg = Utils.deserialize(connection.takeMessage());
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
        tasks.add(task);
    }

    public void sendBroadcast(String message) {
        connections.forEach((id, client) -> sendOne(id, message));
    }

    public void sendOne(UUID clientId, String message) {
        try {
            Connection connection = connections.get(clientId);
            if (connection == null) {
                LOGGER.warn("Unknown connection with id: {}", clientId);
            } else {
                connection.sendMessage(message);
            }
        } catch (IOException e) {
            LOGGER.error("Cannot send message to {}", clientId, e);
        }
    }

    private void closeConnection(UUID id) {
        Connection connection = connections.remove(id);
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                LOGGER.error("Error when closing connection", e);
            }
        }
    }
}