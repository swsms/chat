package org.artb.chat.server.core.command;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.message.MessageSender;

import static org.artb.chat.server.core.message.MsgConstants.NOT_VALID_COMMAND_TEMPLATE;

public class NotValidCommand implements Command {
    private final String enteredContent;
    private final BufferedConnection connection;
    private final MessageSender sender;

    public NotValidCommand(String enteredLine, BufferedConnection connection, MessageSender sender) {
        this.enteredContent = enteredLine;
        this.connection = connection;
        this.sender = sender;
    }

    @Override
    public void execute() {
        String text = String.format(NOT_VALID_COMMAND_TEMPLATE, enteredContent);
        sender.sendPersonal(connection.getId(), Message.newServerMessage(text));
    }
}
