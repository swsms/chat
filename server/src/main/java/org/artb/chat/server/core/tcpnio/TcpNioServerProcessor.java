package org.artb.chat.server.core.tcpnio;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.connection.tcpnio.SwitchKeyInterestOpsTask;
import org.artb.chat.common.connection.tcpnio.TcpNioConnection;
import org.artb.chat.server.core.ServerProcessor;
import org.artb.chat.server.core.event.ConnectionEvent;
import org.artb.chat.server.core.event.ConnectionEventType;
import org.artb.chat.server.core.event.ReceivedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

public class TcpNioServerProcessor extends ServerProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpNioServerProcessor.class);

    private Selector selector;
    private ServerSocketChannel serverSocket;

    private final Queue<SwitchKeyInterestOpsTask> switchTasks = new ConcurrentLinkedQueue<>();

    public TcpNioServerProcessor(String host, int port) {
        super(host, port);
    }

    private void configure() throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();

        serverSocket.bind(new InetSocketAddress(host, port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, OP_ACCEPT);
    }

    @Override
    public void start() {
        if (runningFlag.get()) {
            LOGGER.warn("The server was already started. It will not be started again.");
        } else {
            runningFlag.set(true);

            try {
                configure();
                LOGGER.info("Server started on {}:{}", host, port);
            } catch (IOException e) {
                LOGGER.error("Cannot start server on {}:{}", host, port, e);
                runningFlag.set(false);
            }

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
    }

    @Override
    public void stop() {
        if (!runningFlag.get()) {
            LOGGER.warn("The server is not started yet.");
        } else {
            runningFlag.set(false);
            try {
                connections.keySet().forEach(this::closeConnection);
                serverSocket.close();
            } catch (IOException e) {
                LOGGER.error("Cannot close socket", e);
            }
        }
    }

    private void processKeys() throws IOException {
        int switched = switchKeyInterestOps();
        LOGGER.info("Switched keys count: {}", switched);

        int numKeys = selector.select();
        LOGGER.info("Selected keys number: {}", numKeys);

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
                    clientId, new TcpNioConnection(selector, clientSocket, switchTasks));

            clientSocket.configureBlocking(false);
            clientSocket.register(selector, OP_READ, connection);
        } catch (IOException e) {
            LOGGER.error("Cannot initialize socket for client {}", clientId, e);
            return;
        }

        connections.putIfAbsent(clientId, connection);

        try {
            String remoteAddress = Objects.toString(clientSocket.getRemoteAddress());
            LOGGER.info("Registering new client {} from {}", clientId, remoteAddress);
        } catch (IOException e) {
            LOGGER.warn("Cannot get remote address for {}: {}", clientId, e.getMessage());
        }

        connectionEventListener.accept(new ConnectionEvent(
                connection.getId(), connection, ConnectionEventType.CONNECTED
        ));
    }

    private void read(SelectionKey key) {
        BufferedConnection connection = (BufferedConnection) key.attachment();
        try {
            String incomingData = connection.take();
            LOGGER.info("Incoming data {} on {}", incomingData, connection.getId());
            receivedDataListener.accept(new ReceivedData(connection.getId(), incomingData, connection));
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
                connectionEventListener.accept(new ConnectionEvent(
                        connection.getId(), connection, ConnectionEventType.DISCONNECTED
                ));
            } catch (IOException e) {
                LOGGER.error("Cannot close connection", e);
            }
        }
    }

    private int switchKeyInterestOps() {
        SwitchKeyInterestOpsTask task;
        int switchedCount = 0;
        while ((task = switchTasks.poll()) != null) {
            SelectionKey key = task.getKey();
            if (key != null && key.isValid()) {
                key.interestOps(task.getOps());
                switchedCount++;
            }
        }
        return switchedCount;
    }
}