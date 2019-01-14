package org.artb.chat.server.core.command;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.message.MessageSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.artb.chat.server.core.message.MsgConstants.LEFT_CHAT_TEMPLATE;

public class ExitCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExitCommand.class);

    private final AuthUserStorage storage;
    private final BufferedConnection connection;
    private final MessageSender sender;

    public ExitCommand(AuthUserStorage storage, BufferedConnection connection, MessageSender sender) {
        this.storage = storage;
        this.connection = connection;
        this.sender = sender;
    }

    @Override
    public void execute() {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                LOGGER.error("Cannot close connection", e);
            }
        }

        if (connection.getId() != null && storage.authenticated(connection.getId())) {
            String user = storage.removeUser(connection.getId());
            String text = String.format(LEFT_CHAT_TEMPLATE, user);
            sender.sendBroadcast(Message.newServerMessage(text));
        }
    }
}
