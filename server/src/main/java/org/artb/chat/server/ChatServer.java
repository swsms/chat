package org.artb.chat.server;

import org.artb.chat.common.ChatComponent;
import org.artb.chat.common.Constants;
import org.artb.chat.server.core.ServerProcessor;
import org.artb.chat.server.core.message.BasicMsgSender;
import org.artb.chat.server.core.message.MessageProcessor;
import org.artb.chat.server.core.message.MsgSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.artb.chat.server.core.storage.auth.InMemoryAuthUserStorage;
import org.artb.chat.server.core.storage.history.HistoryStorage;
import org.artb.chat.server.core.storage.history.InMemoryHistoryStorage;
import org.artb.chat.server.core.tcpnio.TcpNioServerProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatServer implements ChatComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final ServerProcessor server;

    private final HistoryStorage historyStorage = new InMemoryHistoryStorage(Constants.HISTORY_SIZE);
    private final AuthUserStorage userStorage = new InMemoryAuthUserStorage();
    private MsgSender sender;
    private MessageProcessor msgProcessor;

    private final AtomicBoolean runningFlag = new AtomicBoolean();

    public ChatServer(String host, int port) {
        this.server = new TcpNioServerProcessor(host, port);
        this.sender = new BasicMsgSender(userStorage, server.getConnections(), historyStorage);
        this.msgProcessor =  new MessageProcessor(
                historyStorage, sender, server.getReceivedDataQueue(), userStorage, runningFlag);
    }

    @Override
    public void start() {
        Thread msgProcessorThread = new Thread(msgProcessor, "msg-processor-thread");
        msgProcessorThread.start();

        Thread serverThread = new Thread(server::start, "main-server-thread");
        serverThread.start();
    }

    @Override
    public void stop() {
        server.stop();
        runningFlag.set(false);
    }
}
