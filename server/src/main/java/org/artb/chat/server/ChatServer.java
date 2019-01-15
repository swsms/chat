package org.artb.chat.server;

import org.artb.chat.common.Constants;
import org.artb.chat.common.Lifecycle;
import org.artb.chat.common.settings.ServerConfig;
import org.artb.chat.server.core.ConnectionManager;
import org.artb.chat.server.core.ServerProcessor;
import org.artb.chat.server.core.command.CommandFactory;
import org.artb.chat.server.core.event.ConnectionEvent;
import org.artb.chat.server.core.ReceivedData;
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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChatServer implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final ServerProcessor server;
    private final BlockingQueue<ConnectionEvent> connectionEvents = new LinkedBlockingQueue<>();
    private final BlockingQueue<ReceivedData> receivedDataQueue = new LinkedBlockingQueue<>();

    private final List<MessageProcessor> messageProcessors;
    private final List<ConnectionManager> connectionManagers;

    private final HistoryStorage history;
    private final AuthUserStorage users;
    private final MessageSender sender;
    private final CommandFactory factory;

    public ChatServer(ServerConfig config) {
        this.server = new TcpNioServerProcessor(config.getHost(), config.getPort());

        this.history = new InMemoryHistoryStorage(Constants.HISTORY_SIZE);
        this.users = new InMemoryAuthUserStorage();
        this.sender = new BasicMessageSender(users, server::acceptData, history);

        this.factory = new CommandFactory(sender, users, server::disconnect);

        this.messageProcessors = Stream
                .generate(() -> new MessageProcessor(history, sender, receivedDataQueue, users, factory))
                .limit(config.getMsgProcessors())
                .collect(Collectors.toList());

        this.connectionManagers = Stream
                .generate(() -> new ConnectionManager(connectionEvents, sender, users))
                .limit(config.getConnectionManagers())
                .collect(Collectors.toList());
    }

    public void start() {
        startRunnables(messageProcessors, "msg-processor");
        startRunnables(connectionManagers, "con-manager");

        server.setConnectionEventListener(connectionEvents::add);
        server.setReceivedDataListener(receivedDataQueue::add);

        startRunnables(Collections.singletonList(server::start), "main-server");
    }

    private void startRunnables(List<? extends Runnable> runnables, String basicThreadName) {
        for (int i = 0; i < runnables.size(); i++) {
            String finalName = basicThreadName + (runnables.size() > 1 ? "-" + i : "");
            Thread thread = new Thread(runnables.get(i), finalName);
            thread.start();
            LOGGER.info("Starting {} thread", finalName);
        }
    }

    public void stop() {
        server.stop();
        messageProcessors.forEach(MessageProcessor::stop);
        connectionManagers.forEach(ConnectionManager::stop);
    }
}
