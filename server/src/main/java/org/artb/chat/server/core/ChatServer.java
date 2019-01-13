package org.artb.chat.server.core;

import org.artb.chat.common.Constants;
import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.connection.tcpnio.TcpNioConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.Utils;
import org.artb.chat.server.core.message.MessageArrivedEvent;
import org.artb.chat.server.core.message.MessageProcessor;
import org.artb.chat.server.core.message.BasicMsgSender;
import org.artb.chat.server.core.message.MsgSender;
import org.artb.chat.server.core.storage.AuthUserStorage;
import org.artb.chat.server.core.storage.HistoryStorage;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static org.artb.chat.server.core.message.MsgConstants.*;

public class ChatServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final String host;
    private final int port;

    private volatile AtomicBoolean runningFlag = new AtomicBoolean();

    private Selector selector;
    private ServerSocketChannel serverSocket;

    private final Map<UUID, BufferedConnection> connections = new ConcurrentHashMap<>();
    private final BlockingQueue<MessageArrivedEvent> events = new LinkedBlockingQueue<>();
    private final AuthUserStorage users = new AuthUserStorage();
    private final HistoryStorage history = new HistoryStorage(Constants.HISTORY_SIZE);
    private MsgSender sender = new BasicMsgSender(users, connections);

    private final MessageProcessor msgProcessor;

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.msgProcessor = new MessageProcessor(
                history, sender, events, users, runningFlag);
    }

    public void start() {
        runningFlag.set(true);

        try {
            configure();
            LOGGER.info("Server started on {}:{}", host, port);
        } catch (IOException e) {
            LOGGER.error("Cannot start server on {}:{}", host, port, e);
            runningFlag.set(false);
        }

        Thread asyncMsgProcessor = new Thread(msgProcessor, "msg-processor-thread");
        asyncMsgProcessor.start();

        try {
            LOGGER.info("Starting process keys loop");
            while (runningFlag.get()) {
                processKeys();
            }
        } catch (IOException e) {
            LOGGER.error("An error occurs while keys processing", e);
            runningFlag.set(false);
        }

        stop();

        LOGGER.info("The server has been stopped");
    }

    private void stop() {
        runningFlag.set(false);
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
        sender.send(clientId, REQUEST_NAME_MSG);

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
            events.add(new MessageArrivedEvent(connection.getId(), msg, connection));
        } catch (IOException e) {
            closeConnection(connection.getId());
        }
    }

    private void write(SelectionKey key) {
        BufferedConnection connection = (BufferedConnection) key.attachment();
        try {
            connection.sendPendingData();
        } catch (IOException e) {
            closeConnection(connection.getId());
        }
    }

    private void closeConnection(UUID id) {
        BufferedConnection connection = connections.remove(id);
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Connection with {} was successfully closed.", id);
            } catch (IOException e) {
                LOGGER.error("Cannot close connection", e);
            }
        }

        if (users.authenticated(id)) {
            String user = users.removeUser(id);
            String text = String.format(LEFT_CHAT_TEMPLATE, user);
            sender.sendBroadcast(Message.newServerMessage(text));
        }
    }
}
