package org.artb.chat.client;

import org.artb.chat.client.core.AsyncMessageReader;
import org.artb.chat.client.ui.UIConsoleDisplay;
import org.artb.chat.client.ui.UIDisplay;
import org.artb.chat.common.Constants;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.nio.channels.SelectionKey.*;

public class ChatClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);

    private final String serverHost;
    private final int serverPort;

    private Selector selector;
    private SocketChannel socket;
    private ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);

    private volatile boolean running = true;

    private Queue<Message> messages = new ConcurrentLinkedQueue<>();

    private final UIDisplay display = new UIConsoleDisplay();
    private final AsyncMessageReader asyncMessageReader = new AsyncMessageReader((msg) -> {
        messages.add(msg);
        switchMode(OP_WRITE);
    });

    public ChatClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
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
            running = false;
        }

        stop();
    }

    private void stop() {
        try {
            running = false;
            asyncMessageReader.stop();
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

    private void switchMode(int targetMode) {
        SelectionKey key = socket.keyFor(selector);
        key.interestOps(targetMode);
        selector.wakeup();
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
                    write();
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

            Message msg = Utils.readMessage(buffer);

            display.print(msg);
        } catch (IOException e) {
            LOGGER.error("Cannot read message", e);
            stop();
        }
    }

    private void write() {
        while (!messages.isEmpty()) {
            Message msg = messages.poll();
            try {
                String msgJson = Utils.serialize(msg);
                ByteBuffer buf = ByteBuffer.wrap(msgJson.getBytes(StandardCharsets.UTF_8));
                socket.write(buf);
            } catch (IOException e) {
                LOGGER.error("Cannot send message: {}", msg, e);
            }
        }
        switchMode(OP_READ);
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
}
