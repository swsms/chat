package org.artb.chat.server.core.command;

import org.artb.chat.common.message.MessageType;
import org.artb.chat.server.core.message.MessageSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.artb.chat.server.core.storage.auth.InvalidNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.artb.chat.common.message.MessageFactory.newServerMessage;
import static org.artb.chat.common.message.MessageConstants.SUCCESSFULLY_RENAMED;
import static org.artb.chat.common.message.MessageConstants.USER_IS_RENAMED_TEMPLATE;

public class RenameCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(RenameCommand.class);

    private final UUID userId;
    private final AuthUserStorage storage;
    private final MessageSender sender;

    private final String newName;

    public RenameCommand(UUID userId,
                         String newName,
                         AuthUserStorage storage,
                         MessageSender sender) {

        this.userId = userId;
        this.newName = newName;
        this.storage = storage;
        this.sender = sender;
    }

    @Override
    public void execute() {
        try {
            String oldName = storage.getUserName(userId);
            storage.upsertUserName(userId, newName);

            String text = String.format(USER_IS_RENAMED_TEMPLATE, oldName, newName);
            sender.sendBroadcast(newServerMessage(text));
        } catch (InvalidNameException e) {
            LOGGER.info(e.getMessage());
            sender.sendPersonal(userId, newServerMessage(e.getMessage(), MessageType.NAME_DECLINED));
        }
    }
}
