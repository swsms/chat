package org.artb.chat.server.core.command;

import org.artb.chat.server.core.message.MessageSender;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.artb.chat.common.message.MessageFactory.newServerMessage;

public class HelpCommand implements Command {
    private final UUID userId;
    private final MessageSender sender;

    public HelpCommand(UUID userId, MessageSender sender) {
        this.userId = userId;
        this.sender = sender;
    }

    @Override
    public void execute() {
        List<String> types =
                Arrays.stream(CommandType.values())
                        .map(cmdType -> cmdType.getValue() + "\t" + cmdType.getDesc())
                        .collect(Collectors.toList());

        sender.sendPersonal(userId, newServerMessage(String.join("\n", types)));
    }
}
