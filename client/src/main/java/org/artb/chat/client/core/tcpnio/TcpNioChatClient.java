package org.artb.chat.client.core.tcpnio;

import org.artb.chat.client.core.ChatClient;
import org.artb.chat.client.core.ClientException;
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
import java.util.List;
import java.util.Set;

import static java.nio.channels.SelectionKey.*;

public class TcpNioChatClient extends ChatClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);
    private Selector selector;

    public TcpNioChatClient(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }

    @Override
    protected Connection configureConnection() throws ClientException {
        try {
            selector = Selector.open();

            SocketChannel socket = SocketChannel.open();
            socket.configureBlocking(false);

            socket.connect(new InetSocketAddress(serverHost, serverPort));
            socket.register(selector, OP_CONNECT);

            return new TcpNioConnection(selector, socket);
        } catch (IOException e) {
            LOGGER.info("Cannot configure client", e);
            throw new ClientException(e);
        }
    }

    @Override
    protected void doMainLogic() throws ClientException {
        while(running.get()) {
            int numKeys;
            try {
                numKeys = selector.select();
            } catch (IOException e) {
                LOGGER.error("Cannot select keys", e);
                throw new ClientException(e);
            }

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
                        processRead();
                    } else if (key.isWritable()) {
                        processWrite();
                    } else if (key.isConnectable()) {
                        processConnect();
                    }
                }
            }
        }
    }

    private void processRead() {
        try {
            List<Message> messages = Utils.deserializeList(connection.take());
            messages.forEach(display::print);
        } catch (IOException e) {
            LOGGER.error("Cannot read message", e);
            running.set(false);
        }
    }

    private void processWrite() {
        while (!messages.isEmpty()) {
            Message msg = messages.poll();
            try {
                connection.send(Utils.serialize(msg));
            } catch (IOException e) {
                LOGGER.error("Cannot write message: {}", msg, e);
            }
        }
    }

    private void processConnect() {
        try {
            if (connection.connect()) {
                LOGGER.info("Established connection with {}:{}", serverHost, serverPort);
            } else {
                LOGGER.info("Cannot connect to {}:{}", serverHost, serverPort);
                running.set(false);
            }
        } catch (IOException e) {
            LOGGER.error("Cannot connect to {}:{}", serverHost, serverPort, e);
            running.set(false);
        }
    }
}
