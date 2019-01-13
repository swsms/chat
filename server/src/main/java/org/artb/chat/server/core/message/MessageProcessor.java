package org.artb.chat.server.core.message;

import org.artb.chat.common.Utils;
import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.command.Command;
import org.artb.chat.server.core.command.CommandFactory;
import org.artb.chat.server.core.command.CommandParsingException;
import org.artb.chat.server.core.storage.AuthUserStorage;
import org.artb.chat.server.core.storage.HistoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.artb.chat.server.core.message.MsgConstants.*;

public class MessageProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

    private final HistoryStorage historyStorage;
    private final MsgSender sender;
    private final BlockingQueue<MessageArrivedEvent> events;
    private final AuthUserStorage userStorage;

    private final AtomicBoolean runningFlag;

    public MessageProcessor(HistoryStorage historyStorage,
                            MsgSender sender,
                            BlockingQueue<MessageArrivedEvent> events,
                            AuthUserStorage storage,
                            AtomicBoolean runningFlag) {

        this.historyStorage = historyStorage;
        this.sender = sender;
        this.events = events;
        this.userStorage = storage;
        this.runningFlag = runningFlag;
    }

    @Override
    public void run() {
        while (runningFlag.get()) {
            final MessageArrivedEvent event;
            try {
                event = events.take();
            } catch (InterruptedException e) {
                LOGGER.error("Cannot take a message from queue", e);
                return;
            }

            Message incomingMessage = event.getMessage();
            UUID clientId = event.getClientId();
            String msgContent = incomingMessage.getContent().trim();

            if (userStorage.authenticated(clientId)) {
                if (msgContent.startsWith(Command.CMD_CHAR)) {
                    executeCommandForConnection(event.getConnection(), msgContent);
                } else {
                    broadcastProcessedMessage(clientId, msgContent);
                }
            } else {
                tryAuthenticate(clientId, msgContent);
            }
        }
        LOGGER.info("Successfully stopped");
    }

    private void executeCommandForConnection(BufferedConnection connection, String content) {
        CommandFactory factory = new CommandFactory(connection, sender, userStorage);
        try {
            Command command = factory.createCommand(content);
            command.execute();
        } catch (CommandParsingException e) {
            LOGGER.error("Cannot parse command", e);
        }
    }

    private void broadcastProcessedMessage(UUID id, String text) {
        Message msg = new Message();

        msg.setContent(text);
        msg.setSender(userStorage.getUserName(id));
        msg.setType(Message.Type.USER_TEXT);
        msg.setServed(ZonedDateTime.now());

        historyStorage.add(msg);

        sender.sendBroadcast(msg);
    }

    private void tryAuthenticate(UUID clientId, String userName) {
        if (Utils.isBlank(userName)) {
            sender.send(clientId, NAME_DECLINED_MSG);
        } else if (userStorage.containsUserName(userName)) {
            sender.send(clientId, NAME_ALREADY_IN_USE_MSG);
        } else {
            userStorage.saveUser(clientId, userName);

            String loggedText = String.format(SUCCESSFULLY_LOGGED_TEMPLATE, userName);
            sender.send(clientId, Message.newServerMessage(loggedText));

            String readyText = String.format(READY_TO_CHATTING, userName);
            sender.sendBroadcast(Message.newServerMessage(readyText));

            List<Message> history = historyStorage.history();
            LOGGER.info("Sent history with size {} entries.", history.size());
            sender.send(clientId, history);
        }
    }
}
