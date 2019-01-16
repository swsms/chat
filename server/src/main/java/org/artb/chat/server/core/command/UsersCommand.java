package org.artb.chat.server.core.command;

import org.artb.chat.common.Utils;
import org.artb.chat.common.command.CommandInfo;
import org.artb.chat.common.message.MessageType;
import org.artb.chat.server.core.message.MessageSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.artb.chat.common.message.MessageFactory.newServerMessage;

public class UsersCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersCommand.class);

    private final UUID userId;
    private final AuthUserStorage storage;
    private final MessageSender sender;

    public UsersCommand(UUID userId, AuthUserStorage storage, MessageSender sender) {
        this.userId = userId;
        this.storage = storage;
        this.sender = sender;
    }

    @Override
    public void execute() {
        List<String> users =
                storage.getUsers().values().stream()
                        .sorted().collect(Collectors.toList());

        try {
            String json = Utils.serializeList(users);
            sender.sendPersonal(userId, newServerMessage(json, MessageType.USER_LIST));
        } catch (IOException e) {
            LOGGER.error("Cannot serialize users", e);
        }
    }
}
