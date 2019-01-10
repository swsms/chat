package org.artb.chat.client;

import org.artb.chat.common.message.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

import static java.nio.channels.SelectionKey.*;

public class ChatClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);

    private final String serverHost;
    private final int serverPort;

    private Selector selector;

    private SocketChannel socket;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    private volatile boolean running = true;

    public ChatClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void start() {
        running = true;
        try {
            configure();

            while (running) {
                processKeys();
            }
        } catch (IOException e) {
            LOGGER.error("Cannot connect to {}:{}", serverHost, serverPort, e);
            running = false;
        }

        stop();
    }

    private void stop() {
        try {
            running = false;
            socket.close();
            LOGGER.info("Client successfully stopped");
        } catch (IOException e) {
            LOGGER.error("Cannot close socket", e);
        }
    }

    private void configure() throws IOException {
        selector = Selector.open();

        socket = SocketChannel.open();
        socket.configureBlocking(false);

        socket.connect(new InetSocketAddress(serverHost, serverPort));
        socket.register(selector, OP_CONNECT);
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
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                } else if (key.isConnectable()) {
                    connect(key);
                }
            }
        }
    }

    private void read(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();

        try {
            client.read(buffer);
            if (client.read(buffer) < 0) {
                stop();
            }
        } catch (IOException e) {
            stop();
            return;
        }

        String messageJson = (new String(Utils.extractDataFromBuffer(buffer), StandardCharsets.UTF_8)).trim();
        LOGGER.info(messageJson);

        key.interestOps(OP_WRITE);
    }

    private void connect(SelectionKey key) {
        try {
            if (socket.finishConnect()) {
                LOGGER.info("Established connection with {}:{}", serverHost, serverPort);
                key.interestOps(OP_READ);
            } else {
                LOGGER.info("Cannot connect to {}:{}", serverHost, serverPort);
                running = false;
            }
        } catch (IOException e) {
            LOGGER.error("Cannot connect to {}:{}", serverHost, serverPort, e);
            running = false;
        }
    }

    private void write(SelectionKey key) {
        LOGGER.info("Writable");
        key.interestOps(OP_READ);
    }
}
