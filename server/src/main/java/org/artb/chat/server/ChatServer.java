package org.artb.chat.server;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.connection.Connection;
import org.artb.chat.common.connection.tcpnio.TcpNioConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.Utils;
import org.artb.chat.server.core.message.BasicMsgSender;
import org.artb.chat.server.core.message.MsgSender;
import org.artb.chat.server.core.task.AsyncTaskProcessor;
import org.artb.chat.server.core.task.SendingTask;
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
import static org.artb.chat.server.core.message.MsgConstants.*;

public class ChatServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final String host;
    private final int port;

    private volatile boolean running = false;

    private final BlockingQueue<SendingTask> tasks = new LinkedBlockingQueue<>();
    private final AsyncTaskProcessor taskProcessor = new AsyncTaskProcessor(this, tasks);

    private Selector selector;
    private ServerSocketChannel serverSocket;

    private final Map<UUID, BufferedConnection> connections = new ConcurrentHashMap<>();
    private MsgSender sender = new BasicMsgSender(connections);

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
                } else if (key.isWritable()) {
                    write(key);
                }
            }
        }
    }

    private void register(SelectionKey key) {
        UUID clientId = UUID.randomUUID();

        final SocketChannel clientSocket;
        final BufferedConnection connection;
        try {
            clientSocket = ((ServerSocketChannel) key.channel()).accept();

            connection = new BufferedConnection(
                    clientId, new TcpNioConnection(selector, clientSocket));

            clientSocket.configureBlocking(false);
            clientSocket.register(selector, OP_READ, connection);
        } catch (IOException e) {
            LOGGER.error("Cannot register new client with id {}", clientId, e);
            return;
        }

        connections.putIfAbsent(clientId, connection);
        sender.sendOne(clientId, REQUEST_NAME_MSG);

        try {
            String remoteAddress = Objects.toString(clientSocket.getRemoteAddress());
            LOGGER.info("New client {} accepted from {}", clientId, remoteAddress);
        } catch (IOException e) {
            LOGGER.warn("Cannot get remote address for {}: {}", clientId, e.getMessage());
        }
    }

    private void read(SelectionKey key) {
        BufferedConnection connection = (BufferedConnection) key.attachment();
        try {
            Message msg = Utils.deserialize(connection.take());
            LOGGER.info("Incoming message: {}", msg);
            if (connection.isAuthenticated()) {
                msg.setSender(connection.getUserName());
                sender.sendAll(msg);
            } else {
                String userName = msg.getContent();
                if (Utils.isBlank(userName)) {
                    sender.sendOne(connection.getId(), REQUEST_NAME_MSG);
                } else if ("server".equalsIgnoreCase(userName)) {
                    sender.sendOne(connection.getId(), NAME_DECLINED_MSG);
                } else {
                    connection.setUserName(userName);
                    sender.sendOne(connection.getId(), NAME_ACCEPTED_MSG);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Cannot send message", e);
            closeConnection(connection.getId());
        }
    }


    private void write(SelectionKey key) {
        BufferedConnection connection = (BufferedConnection) key.attachment();
        try {
            connection.sendPendingData();
        } catch (IOException e) {
            LOGGER.error("Cannot send message to {}", connection.getId(), e);
            closeConnection(connection.getId());
        }
    }

//    private void enqueue(SendingTask task) {
//        tasks.add(task);
//    }

    private void closeConnection(UUID id) {
        LOGGER.info("Trying to close connection {}", id);
        Connection connection = connections.remove(id);
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                LOGGER.error("Error when closing connection", e);
            }
        }
        sender.sendAll(Message.newServerMessage("Vasily has left the chat"));
    }
}
