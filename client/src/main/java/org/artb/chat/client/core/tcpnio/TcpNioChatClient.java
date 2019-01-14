package org.artb.chat.client.core.tcpnio;

import org.artb.chat.client.core.ChatClient;
import org.artb.chat.client.core.ClientException;
import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.connection.tcpnio.SwitchKeyInterestOpsTask;
import org.artb.chat.common.connection.tcpnio.TcpNioConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.nio.channels.SelectionKey.*;

public class TcpNioChatClient extends ChatClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);
    private Selector selector;
    private final Queue<SwitchKeyInterestOpsTask> switchTasks = new ConcurrentLinkedQueue<>();

    public TcpNioChatClient(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }

    public TcpNioChatClient(String serverHost, int serverPort, InputStream input) {
        super(serverHost, serverPort, input);
    }

    @Override
    protected BufferedConnection configureConnection() throws ClientException {
        try {
            selector = Selector.open();

            SocketChannel socket = SocketChannel.open();
            socket.configureBlocking(false);

            socket.connect(new InetSocketAddress(serverHost, serverPort));
            socket.register(selector, OP_CONNECT);

            return new BufferedConnection(
                    UUID.randomUUID(),
                    new TcpNioConnection(selector, socket, switchTasks));
        } catch (IOException e) {
            LOGGER.info("Cannot configure client", e);
            throw new ClientException(e);
        }
    }

    @Override
    protected void doMainLogic() throws ClientException {
        while(running.get()) {
            switchKeyInterestOps();
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
            running.set(false);
        }
    }

    private void processWrite() {
        try {
            connection.sendPendingData();
        } catch (IOException e) {
            running.set(false);
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
