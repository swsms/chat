package org.artb.chat.server.core.command;

import org.artb.chat.common.Utils;
import org.artb.chat.common.command.CommandInfo;
import org.artb.chat.common.message.MessageType;
import org.artb.chat.server.core.message.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.artb.chat.common.message.MessageFactory.newServerMessage;

public class HelpCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelpCommand.class);

    private final UUID userId;
    private final MessageSender sender;

    public HelpCommand(UUID userId, MessageSender sender) {
        this.userId = userId;
        this.sender = sender;
    }

    @Override
    public void execute() {
        List<CommandInfo> commands = Arrays.stream(CommandType.values())
                .map(cmd -> new CommandInfo(cmd.getValue(), cmd.getDesc()))
                .collect(Collectors.toList());

        try {
            String json = Utils.serializeList(commands);
            sender.sendPersonal(userId, newServerMessage(json, MessageType.COMMAND_LIST));
        } catch (IOException e) {
            LOGGER.error("Cannot serialize commands", e);
        }
    }
}
