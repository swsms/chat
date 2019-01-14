package org.artb.chat.server.core.command;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.message.MessageSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.artb.chat.server.core.storage.auth.InvalidNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.artb.chat.server.core.message.MsgConstants.USER_IS_RENAMED_TEMPLATE;

// TODO this command needs sync
public class RenameCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(RenameCommand.class);

    private final AuthUserStorage storage;
    private final BufferedConnection connection;
    private final MessageSender sender;

    private final String newName;

    public RenameCommand(String newName,
                         AuthUserStorage storage,
                         BufferedConnection connection,
                         MessageSender sender) {

        this.newName = newName;
        this.storage = storage;
        this.connection = connection;
        this.sender = sender;
    }

    @Override
    public void execute() {
        try {
            String oldName = storage.getUserName(connection.getId());
            storage.upsertUserName(connection.getId(), newName);

            String text = String.format(USER_IS_RENAMED_TEMPLATE, oldName, newName);
            sender.sendBroadcast(Message.newServerMessage(text));
        } catch (InvalidNameException e) {
            LOGGER.info(e.getMessage());
            sender.sendPersonal(connection.getId(), Message.newServerMessage(e.getMessage()));
        }
    }
}
