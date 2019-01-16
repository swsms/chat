package org.artb.chat.server.core.transport.tcpnio;

import org.artb.chat.common.transport.Connection;
import org.artb.chat.common.transport.tcpnio.NioUtils;
import org.artb.chat.common.transport.tcpnio.SwitchKeyInterestOpsTask;
import org.artb.chat.common.transport.tcpnio.TcpNioConnection;
import org.artb.chat.server.core.transport.ServerProcessor;
import org.artb.chat.server.core.event.ConnectionEvent;
import org.artb.chat.server.core.event.ConnectionEventType;
import org.artb.chat.server.core.ReceivedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

public class TcpNioServerProcessor extends ServerProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpNioServerProcessor.class);

    private Selector selector;
    private ServerSocketChannel serverSocket;

    private final Map<UUID, Connection> connections = new ConcurrentHashMap<>();
    private final Queue<SwitchKeyInterestOpsTask> switchTasks = new ConcurrentLinkedQueue<>();

    public TcpNioServerProcessor(String host, int port,
                                 Consumer<ConnectionEvent> connectionEventListener,
                                 Consumer<ReceivedData> receivedDataListener) {
        super(host, port, connectionEventListener, receivedDataListener);
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
        if (running) {
            LOGGER.warn("The server was already started. It will not be started again.");
        } else {
            running = true;

            try {
                configure();
                LOGGER.info("Server started on {}:{}", host, port);
            } catch (IOException e) {
                LOGGER.error("Cannot start server on {}:{}", host, port, e);
                running = false;
            }

            try {
                LOGGER.info("Start processing keys");
                processKeys();
            } catch (IOException e) {
                LOGGER.error("An error occurs while processing keys", e);
            } finally {
                stop();
            }
        }
    }

    @Override
    public void stop() {
        if (!running) {
            LOGGER.warn("The server is not started");
        } else {
            try {
                running = false;
                connections.keySet().forEach(this::disconnect);
                serverSocket.close();
                LOGGER.info("The server has been successfully stopped");
            } catch (IOException e) {
                LOGGER.error("Cannot close socket", e);
            }
        }
    }

    @Override
    public void acceptData(UUID clientId, String data) {
        Connection connection = connections.get(clientId);
        connection.putInBuffer(data);
        connection.notification();
    }

    private void processKeys() throws IOException {
        while (running) {
            NioUtils.switchKeyInterestOps(switchTasks);

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
    }

    private void register(SelectionKey key) {
        UUID clientId = UUID.randomUUID();

        final SocketChannel clientSocket;
        final Connection connection;
        try {
            clientSocket = ((ServerSocketChannel) key.channel()).accept();
            connection = new TcpNioConnection(clientId, selector, clientSocket, switchTasks::add);

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

        connectionEventListener.accept(new ConnectionEvent(clientId, ConnectionEventType.CONNECTED));
    }

    private void read(SelectionKey key) {
        Connection connection = (Connection) key.attachment();
        try {
            String incomingData = connection.take();
            LOGGER.info("Incoming data {} on {}", incomingData, connection.getId());
            receivedDataListener.accept(new ReceivedData(connection.getId(), incomingData));
        } catch (IOException e) {
            disconnect(connection.getId());
        }
    }

    private void write(SelectionKey key) {
        Connection connection = (Connection) key.attachment();
        try {
            connection.flush();
        } catch (IOException e) {
            disconnect(connection.getId());
        }
    }

    @Override
    public void disconnect(UUID id) {
        Connection connection = connections.remove(id);
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Connection with {} was successfully closed.", id);
                connectionEventListener.accept(
                        new ConnectionEvent(id, ConnectionEventType.DISCONNECTED
                ));
            } catch (IOException e) {
                LOGGER.error("Cannot close connection", e);
            }
        }
    }
}
