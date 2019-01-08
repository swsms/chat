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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

public class ChatServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final String host;
    private final int port;

    private Selector selector;
    private ServerSocketChannel socket;

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        selector = Selector.open();
        socket = ServerSocketChannel.open();

        socket.socket().setReuseAddress(true);
        socket.bind(new InetSocketAddress(host, port));
        socket.configureBlocking(false);
        socket.register(selector, OP_ACCEPT);

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
                        register(selector, socket);
                    }
                }
            }
        }
    }

    private void register(Selector selector, ServerSocketChannel socket)
            throws IOException {

        LOGGER.info("A new client is coming");
        SocketChannel client = socket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }
}
