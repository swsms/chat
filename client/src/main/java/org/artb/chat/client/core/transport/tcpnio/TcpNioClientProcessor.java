package org.artb.chat.client.core.transport.tcpnio;

import org.artb.chat.client.core.transport.ClientProcessor;
import org.artb.chat.common.transport.BufferedConnection;
import org.artb.chat.common.transport.tcpnio.NioUtils;
import org.artb.chat.common.transport.tcpnio.SwitchKeyInterestOpsTask;
import org.artb.chat.common.transport.tcpnio.TcpNioConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.nio.channels.SelectionKey.*;

public class TcpNioClientProcessor extends ClientProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpNioClientProcessor.class);

    private Selector selector;
    private BufferedConnection connection;

    private final Queue<SwitchKeyInterestOpsTask> switchTasks = new ConcurrentLinkedQueue<>();

    public TcpNioClientProcessor(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }

    private void configure() throws IOException {
        selector = Selector.open();

        SocketChannel socket = SocketChannel.open();
        socket.configureBlocking(false);

        socket.connect(new InetSocketAddress(serverHost, serverPort));
        socket.register(selector, OP_CONNECT);

        this.connection = new BufferedConnection(
                new TcpNioConnection(UUID.randomUUID(), selector, socket, switchTasks));
    }

    public void start() {
        if (running) {
            LOGGER.warn("The processor was already started. It will not be started again.");
        } else {
            running = true;

            try {
                configure();
            } catch (IOException e) {
                LOGGER.error("Cannot connect to {}:{}", serverHost, serverPort, e);
                running = false;
            }

            try {
                processKeys();
            } catch (IOException e) {
                LOGGER.error("An error occurs while processing keys", e);
            } finally {
                stop();
            }
        }
    }

    public synchronized void stop() {
        try {
            running = false;
            if (connection != null) {
                connection.close();
            }
        } catch (IOException e) {
            LOGGER.error("Cannot close socket", e);
        } finally {
            handler.handle();
        }
    }

    @Override
    public void acceptData(String data) {
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
    }

    private void read() {
        try {
            String data = connection.take();
            receivedDataListener.accept(data);
        } catch (IOException e) {
            running = false;
        }
    }

    private void write() {
        try {
            connection.flush();
        } catch (IOException e) {
            running = false;
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
