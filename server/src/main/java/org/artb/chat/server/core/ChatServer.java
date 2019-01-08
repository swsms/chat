package org.artb.chat.server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

public class ChatServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final String host;
    private final int port;

    private Selector selector;
    private ServerSocketChannel serverSocket;

    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();

        serverSocket.socket().setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(host, port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, OP_ACCEPT);

        LOGGER.info("Server started on {}:{}", host, port);

        while (true) {
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
                        register(selector);
                    } else if (key.isReadable()) {
                        echo(key);
                    }
                }
            }
        }
    }

    private void register(Selector selector) {
        try {
            SocketChannel client = serverSocket.accept();
            client.configureBlocking(false);
            client.register(selector, OP_READ);
            LOGGER.info("A new client registered with");
        } catch (Exception e) {
            LOGGER.error("Can't register new client", e);
        }
    }

    private void echo(SelectionKey key) throws IOException {
        LOGGER.info("Echo invoked");
        SocketChannel client = (SocketChannel) key.channel();

        Charset cset = Charset.forName("UTF-8");
        if (client.read(buffer) < 0) {
            return;
        }

        buffer.flip();
        cset.decode(buffer);

        buffer.flip();
        client.write(buffer);

        client.close();
    }


}
