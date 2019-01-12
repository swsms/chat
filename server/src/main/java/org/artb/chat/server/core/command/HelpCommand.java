package org.artb.chat.server.core.command;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.message.MsgSender;

public class HelpCommand implements Command {
    private final BufferedConnection connection;
    private final MsgSender sender;

    public HelpCommand(BufferedConnection connection, MsgSender sender) {
        this.connection = connection;
        this.sender = sender;
    }

    @Override
    public void execute() {
        StringBuilder builder = new StringBuilder();

        CommandType[] types = CommandType.values();
        for (CommandType type : types) {
            builder.append(type.getValue() + "\t" + type.getDesc() + "\n");
        }

        sender.send(connection.getId(), Message.newServerMessage(builder.toString()));
    }
}
