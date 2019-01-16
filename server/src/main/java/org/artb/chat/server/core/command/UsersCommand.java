package org.artb.chat.server.core.command;

import org.artb.chat.common.message.MessageType;
import org.artb.chat.server.core.message.MessageSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.artb.chat.common.message.MessageFactory.newServerMessage;

public class UsersCommand implements Command {
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

        sender.sendPersonal(userId, newServerMessage(Objects.toString(users), MessageType.USER_LIST));
    }
}
