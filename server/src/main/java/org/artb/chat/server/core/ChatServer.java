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

public class ChatServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final String host;
    private final int port;

    private Selector selector;
    private ServerSocketChannel serverSocket;

    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    private Map<UUID, SocketChannel> connections = new ConcurrentHashMap<>();

    private BlockingQueue<Message> messages = new LinkedBlockingQueue<>();

    private volatile boolean running = false;

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();

//        serverSocket.socket().setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(host, port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, OP_ACCEPT);

        LOGGER.info("Server started on {}:{}", host, port);
        running = true;

        Thread connectionProcessor = new Thread(() -> {
            LOGGER.info("Connection processor started");
            while (running) {
                try {
                    Message message = messages.take();
                    LOGGER.info("Message to send: {}", message);
                    String msgJson = Utils.serialize(message);
                    if (message.getType() == Message.Type.SERVER_TEXT) {
                        sendOne(message.getClient(), msgJson);
                    } else {
                        sendBroadcast(msgJson);
                    }
                } catch (Exception e) {
                    LOGGER.error("Cannot send message", e);
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
                        register(key);
                    } else if (key.isReadable()) {
                        read(key);
                    }
                }
            }
        }
    }

    public void stop() {
        running = false;
        connections.forEach((id, connection) -> closeConnection(connection));
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.error("Cannot close server socket: ", e.getMessage());
        }
    }

    private void register(SelectionKey key) {
        try {
            SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
            client.configureBlocking(false);

            UUID clientId = UUID.randomUUID();
            client.register(selector, OP_READ, new Session(clientId));

            connections.putIfAbsent(clientId, client);

            String remoteAddress = Objects.toString(client.getRemoteAddress());
            LOGGER.info("New client from {} registered with id {}", remoteAddress, clientId);

            messages.add(Message.newServerMessage(Constants.REQUEST_NAME_MESSAGE, clientId));
        } catch (IOException e) {
            LOGGER.error("Cannot register new client", e);
        }
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

        String messageJson = (new String(extractDataFromBuffer(buffer), StandardCharsets.UTF_8)).trim();
        Session session = (Session) key.attachment();

        try {
            Message msg = Utils.deserialize(messageJson);
            LOGGER.info("{}", msg);
            if (session.isAuth()) {
                messages.add(msg);
            } else {
                String userName = msg.getContent();
                if (Utils.isBlank(userName)) {
                    messages.add(Message.newServerMessage(Constants.REQUEST_NAME_MESSAGE, session.getClientId()));
                } else {
                    session.setName(userName);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Incorrect message received: {}", messageJson, e);
            messages.add(Message.newServerMessage("Incorrect message", session.getClientId()));
        }
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

    private byte[] extractDataFromBuffer(ByteBuffer buffer) {
        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        buffer.clear();
        return bytes;
    }

    private void closeConnection(SocketChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            LOGGER.error("Error when closing connection", e);
        }
    }
}
