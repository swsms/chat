package org.artb.chat.server;

import org.artb.chat.common.ChatComponent;
import org.artb.chat.common.Constants;
import org.artb.chat.server.core.ConnectionManager;
import org.artb.chat.server.core.ServerProcessor;
import org.artb.chat.server.core.event.ConnectionEvent;
import org.artb.chat.server.core.event.ReceivedData;
import org.artb.chat.server.core.message.BasicMessageSender;
import org.artb.chat.server.core.message.MessageProcessor;
import org.artb.chat.server.core.message.MessageSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.artb.chat.server.core.storage.auth.InMemoryAuthUserStorage;
import org.artb.chat.server.core.storage.history.HistoryStorage;
import org.artb.chat.server.core.storage.history.InMemoryHistoryStorage;
import org.artb.chat.server.core.tcpnio.TcpNioServerProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatServer implements ChatComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final ServerProcessor server;
    private final BlockingQueue<ConnectionEvent> connectionEventsQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ReceivedData> receivedDataQueue = new LinkedBlockingQueue<>();

    private final MessageProcessor msgProcessor;
    private final ConnectionManager manager;

    private final AtomicBoolean runningFlag = new AtomicBoolean();

    public ChatServer(String host, int port) {
        this.server = new TcpNioServerProcessor(host, port);

        HistoryStorage historyStorage = new InMemoryHistoryStorage(Constants.HISTORY_SIZE);
        AuthUserStorage userStorage = new InMemoryAuthUserStorage();
        MessageSender sender = new BasicMessageSender(userStorage, server::acceptData, historyStorage);

        this.msgProcessor = new MessageProcessor(
                historyStorage, sender, receivedDataQueue, userStorage, runningFlag);

        this.manager = new ConnectionManager(connectionEventsQueue, runningFlag, sender, userStorage);
    }

    @Override
    public void start() {
        runningFlag.set(true);

        Thread msgProcessorThread = new Thread(msgProcessor, "msg-processor-thread");
        msgProcessorThread.start();

        Thread connectionManager = new Thread(manager, "connection-manager-thread");
        connectionManager.start();

        server.setConnectionEventListener(connectionEventsQueue::add);
        server.setReceivedDataListener(receivedDataQueue::add);

        Thread serverThread = new Thread(server::start, "main-server-thread");
        serverThread.start();
    }

    @Override
    public void stop() {
        server.stop();
        runningFlag.set(false);
    }
}
