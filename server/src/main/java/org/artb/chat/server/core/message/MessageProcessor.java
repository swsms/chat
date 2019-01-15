package org.artb.chat.server.core.message;

import org.artb.chat.common.Utils;
import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.command.Command;
import org.artb.chat.server.core.command.CommandFactory;
import org.artb.chat.server.core.command.CommandParsingException;
import org.artb.chat.server.core.event.ReceivedData;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.artb.chat.server.core.storage.auth.InvalidNameException;
import org.artb.chat.server.core.storage.history.HistoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static org.artb.chat.server.core.message.MsgConstants.*;

public class MessageProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

    private final HistoryStorage historyStorage;
    private final MessageSender sender;
    private final BlockingQueue<ReceivedData> events;
    private final AuthUserStorage userStorage;

    private volatile boolean running;

    public MessageProcessor(HistoryStorage historyStorage,
                            MessageSender sender,
                            BlockingQueue<ReceivedData> events,
                            AuthUserStorage storage) {

        this.historyStorage = historyStorage;
        this.sender = sender;
        this.events = events;
        this.userStorage = storage;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            final ReceivedData receivedData;
            try {
                receivedData = events.take();
            } catch (InterruptedException e) {
                LOGGER.error("Cannot take a message from queue", e);
                return;
            }

            final List<Message> incomingMessages;
            try {
                incomingMessages = Utils.deserializeList(receivedData.getRawData());
            } catch (IOException e) {
                LOGGER.error("Cannot deserialize message: {}", receivedData.getRawData(), e);
                return;
            }

            incomingMessages.forEach((msg) -> {
                UUID clientId = receivedData.getClientId();
                String msgContent = msg.getContent().trim();

                if (userStorage.authenticated(clientId)) {
                    if (msgContent.startsWith(Command.CMD_CHAR)) {
                        executeCommandForConnection(receivedData.getConnection(), msgContent);
                    } else {
                        broadcastProcessedMessage(clientId, msgContent);
                    }
                } else {
                    tryAuthenticate(clientId, msgContent);
                }
            });
        }
        LOGGER.info("Successfully stopped");
    }

    public void stop() {
        running = false;
    }

    private void executeCommandForConnection(BufferedConnection connection, String content) {
        CommandFactory factory = new CommandFactory(connection, sender, userStorage);
        try {
            Command command = factory.createCommand(content);
            command.execute();
        } catch (CommandParsingException e) {
            LOGGER.warn("Cannot parse command: ", e.getMessage());
            sender.sendPersonal(connection.getId(), Message.newServerMessage(NO_PARAMETERS_FOR_COMMAND));
        }
    }

    private void broadcastProcessedMessage(UUID id, String text) {
        Message msg = new Message();

        msg.setContent(text);
        msg.setSender(userStorage.getUserName(id));
        msg.setType(Message.Type.USER_TEXT);
        msg.setServed(ZonedDateTime.now());

        sender.sendBroadcast(msg);
    }

    private void tryAuthenticate(UUID clientId, String userName) {
        try {
            userStorage.upsertUserName(clientId, userName);
        } catch (InvalidNameException e) {
            LOGGER.info(e.getMessage());
            sender.sendPersonal(clientId, Message.newServerMessage(e.getMessage()));
            return;
        }

        List<Message> history = historyStorage.history();
        LOGGER.info("Sent history with size {} entries.", history.size());
        sender.sendPersonal(clientId, history);

        String readyText = String.format(READY_TO_CHATTING_TEMPLATE, userName);
        sender.sendBroadcast(Message.newServerMessage(readyText));

        String loggedText = String.format(SUCCESSFULLY_LOGGED_TEMPLATE, userName);
        Message loggedMsg = Message.newServerMessage(loggedText);

        sender.sendPersonal(clientId, loggedMsg);
    }
}
