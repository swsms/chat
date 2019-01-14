package org.artb.chat.server.core.command;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.message.MessageSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;

import java.util.List;
import java.util.stream.Collectors;

import static org.artb.chat.server.core.message.MsgConstants.*;

public class UsersCommand implements Command {

    private final AuthUserStorage storage;
    private final BufferedConnection connection;
    private final MessageSender sender;

    public UsersCommand(AuthUserStorage storage, BufferedConnection connection, MessageSender sender) {
        this.storage = storage;
        this.connection = connection;
        this.sender = sender;
    }

    @Override
    public void execute() {
        List<String> users =
                storage.getUsers().values().stream()
                        .sorted().collect(Collectors.toList());

        String joinedUsers = String.join("\n", users);

        String text = users.size() != 1 ?
                String.format(LIST_OF_USERS_TEMPLATE, users.size(), joinedUsers) :
                USER_ALONE_IN_CHAT_TEXT;

        sender.sendPersonal(connection.getId(), Message.newServerMessage(text));
    }
}
