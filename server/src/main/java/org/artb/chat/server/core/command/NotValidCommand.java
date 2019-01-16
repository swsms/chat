package org.artb.chat.server.core.command;

import org.artb.chat.common.message.Message;
import org.artb.chat.common.message.MessageType;
import org.artb.chat.server.core.message.MessageSender;

import java.util.UUID;

import static org.artb.chat.common.message.MessageFactory.newServerMessage;
import static org.artb.chat.server.core.message.MessageConstants.NOT_VALID_COMMAND_TEMPLATE;

public class NotValidCommand implements Command {
    private final UUID userId;
    private final String enteredContent;
    private final MessageSender sender;

    public NotValidCommand(UUID userId, String enteredLine, MessageSender sender) {
        this.userId = userId;
        this.enteredContent = enteredLine;
        this.sender = sender;
    }

    @Override
    public void execute() {
        String text = String.format(NOT_VALID_COMMAND_TEMPLATE, enteredContent);
        sender.sendPersonal(userId, newServerMessage(text, MessageType.INCORRECT_COMMAND));
    }
}
