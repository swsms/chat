package org.artb.chat.server.core.message;

import org.artb.chat.common.Utils;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.message.MessageType;
import org.artb.chat.server.core.command.Command;
import org.artb.chat.server.core.command.CommandFactory;
import org.artb.chat.server.core.command.CommandParsingException;
import org.artb.chat.server.core.ReceivedData;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.artb.chat.server.core.storage.auth.InvalidNameException;
import org.artb.chat.server.core.storage.history.HistoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static org.artb.chat.common.message.MessageFactory.newServedUserMessage;
import static org.artb.chat.common.message.MessageFactory.newServerMessage;
import static org.artb.chat.common.message.MessageConstants.*;

public class MessageProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

    private final HistoryStorage historyStorage;
    private final MessageSender sender;
    private final AuthUserStorage userStorage;
    private final CommandFactory factory;

    private final BlockingQueue<ReceivedData> events;
    private volatile boolean running;

    public MessageProcessor(HistoryStorage historyStorage,
                            MessageSender sender,
                            BlockingQueue<ReceivedData> events,
                            AuthUserStorage storage,
                            CommandFactory factory) {

        this.historyStorage = historyStorage;
        this.sender = sender;
        this.events = events;
        this.userStorage = storage;
        this.factory = factory;
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
                incomingMessages = Utils.deserializeMessageList(receivedData.getRawData());
            } catch (IOException e) {
                LOGGER.error("Cannot deserialize message: {}", receivedData.getRawData(), e);
                return;
            }

            incomingMessages.forEach((msg) -> {
                UUID userId = receivedData.getClientId();
                String msgContent = msg.getContent().trim();

                if (userStorage.authenticated(userId)) {
                    if (msgContent.startsWith(Command.CMD_CHAR)) {
                        createAndExecuteCommand(userId, msgContent);
                    } else {
                        String userName = userStorage.getUserName(userId);
                        sender.sendBroadcast(newServedUserMessage(msgContent, userName));
                    }
                } else {
                    tryAuthenticate(userId, msgContent);
                }
            });
        }
        LOGGER.info("Successfully stopped");
    }

    public void stop() {
        running = false;
    }

    private void createAndExecuteCommand(UUID userId, String content) {
        try {
            Command command = factory.createCommandForUser(userId, content);
            command.execute();
        } catch (CommandParsingException e) {
            LOGGER.warn("Cannot parse command: ", e.getMessage());
            sender.sendPersonal(userId, newServerMessage(CMD_NO_PARAMETERS, MessageType.INCORRECT_COMMAND));
        }
    }

    private void tryAuthenticate(UUID clientId, String userName) {
        try {
            userStorage.upsertUserName(clientId, userName);
        } catch (InvalidNameException e) {
            LOGGER.info(e.getMessage());
            sender.sendPersonal(clientId, newServerMessage(e.getMessage(), MessageType.NEED_AUTH));
            return;
        }

        List<Message> history = historyStorage.history();
        history.forEach(msg -> sender.sendPersonal(clientId, msg));
        LOGGER.info("Sent history with size {} entries.", history.size());

        String loggedText = String.format(SUCCESSFULLY_LOGGED_TEMPLATE, userName);
        sender.sendPersonal(clientId, newServerMessage(loggedText, MessageType.SUCCESS_AUTH));

        String readyText = String.format(READY_TO_CHATTING_TEMPLATE, userName);
        sender.sendBroadcast(newServerMessage(readyText));
    }
}
