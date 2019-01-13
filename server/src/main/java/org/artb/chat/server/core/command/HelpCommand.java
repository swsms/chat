package org.artb.chat.server.core.command;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.message.MsgSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand implements Command {
    private final BufferedConnection connection;
    private final MsgSender sender;

    public HelpCommand(BufferedConnection connection, MsgSender sender) {
        this.connection = connection;
        this.sender = sender;
    }

    @Override
    public void execute() {
        List<String> types =
                Arrays.stream(CommandType.values())
                        .map(cmdType -> cmdType.getValue() + "\t" + cmdType.getDesc())
                        .collect(Collectors.toList());

        sender.sendPersonal(
                connection.getId(),
                Message.newServerMessage(String.join("\n", types)));
    }
}
