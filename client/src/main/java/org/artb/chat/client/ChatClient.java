package org.artb.chat.client;

import org.artb.chat.client.core.AsyncMessageReader;
import org.artb.chat.client.ui.UIConsoleDisplay;
import org.artb.chat.client.ui.UIDisplay;
import org.artb.chat.common.connection.Connection;
import org.artb.chat.common.connection.tcpnio.TcpNioConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.nio.channels.SelectionKey.*;

public class ChatClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);

    private final String serverHost;
    private final int serverPort;

    private Connection connection;
    private volatile boolean running = true;

    private final UIDisplay display = new UIConsoleDisplay();
    private final AsyncMessageReader asyncMessageReader;

    private Queue<Message> messages = new ConcurrentLinkedQueue<>();
    private Selector selector;

    public ChatClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.asyncMessageReader = new AsyncMessageReader((msg) -> {
            messages.add(msg);
            connection.notification();
        });
    }

    public void start() {
        running = true;
        try {
            configure();

            asyncMessageReader.start();

            while (running) {
                processKeys();
            }
        } catch (IOException e) {
            LOGGER.error("Cannot connect to {}:{}", serverHost, serverPort, e);
        } finally {
            stop();
        }
    }

    private void stop() {
        try {
            running = false;
            asyncMessageReader.stop();
            if (connection != null) {
                connection.close();
            }
            LOGGER.info("Client successfully stopped");
        } catch (IOException e) {
            LOGGER.error("Cannot close socket", e);
        }
    }

    private void configure() throws IOException {
        selector = Selector.open();

        SocketChannel socket = SocketChannel.open();
        socket.configureBlocking(false);

        socket.connect(new InetSocketAddress(serverHost, serverPort));
        socket.register(selector, OP_CONNECT);

        connection = new TcpNioConnection(selector, socket);
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

                if (key.isReadable()) {
                    read();
                } else if (key.isWritable()) {
                    write();
                } else if (key.isConnectable()) {
                    connect();
                }
            }
        }
    }

    private void read() {
        try {
            Message msg = Utils.deserialize(connection.takeMessage());
            display.print(msg);
        } catch (IOException e) {
            LOGGER.error("Cannot read message", e);
            running = false;
        }
    }

    private void write() {
        while (!messages.isEmpty()) {
            Message msg = messages.poll();
            try {
                connection.sendMessage(Utils.serialize(msg));
            } catch (IOException e) {
                LOGGER.error("Cannot send message: {}", msg, e);
            }
        }
    }

    private void connect() {
        try {
            if (connection.connect()) {
                LOGGER.info("Established connection with {}:{}", serverHost, serverPort);
            } else {
                LOGGER.info("Cannot connect to {}:{}", serverHost, serverPort);
                running = false;
            }
        } catch (IOException e) {
            LOGGER.error("Cannot connect to {}:{}", serverHost, serverPort, e);
            running = false;
        }
    }
}
