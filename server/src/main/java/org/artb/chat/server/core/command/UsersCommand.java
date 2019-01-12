package org.artb.chat.server.core.command;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.message.MsgConstants;
import org.artb.chat.server.core.message.MsgSender;
import org.artb.chat.server.core.storage.AuthUserStorage;

import java.util.List;
import java.util.stream.Collectors;

import static org.artb.chat.server.core.message.MsgConstants.*;

public class UsersCommand implements Command {

    private final AuthUserStorage storage;
    private final BufferedConnection connection;
    private final MsgSender sender;

    public UsersCommand(AuthUserStorage storage, BufferedConnection connection, MsgSender sender) {
        this.storage = storage;
        this.connection = connection;
        this.sender = sender;
    }

    @Override
    public void execute() {
        List<String> users =
                storage.getUsers().stream()
                        .sorted().collect(Collectors.toList());

        String text = String.format(
                LIST_OF_USERS_TEMPLATE, users.size(), String.join("\n", users));

        sender.send(connection.getId(), Message.newServerMessage(text));
    }
}
