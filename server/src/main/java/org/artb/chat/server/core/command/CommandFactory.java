package org.artb.chat.server.core.command;

import org.artb.chat.server.core.message.MessageSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;

import java.util.UUID;
import java.util.function.Consumer;

public class CommandFactory {

    private final MessageSender sender;
    private final AuthUserStorage userStorage;
    private final Consumer<UUID> closer;

    public CommandFactory(MessageSender sender,
                          AuthUserStorage userStorage,
                          Consumer<UUID> closer) {

        this.sender = sender;
        this.userStorage = userStorage;
        this.closer = closer;
    }

    /**
     * It creates a command according to the given string content.
     *
     * @param content the string representation of a command including parameters
     *
     * @return command or a special type DoNothingCommand
     */
    public Command createCommandForUser(UUID userId, String content) throws CommandParsingException {
        String[] cmdWithParams = content.split("\\s+");

        CommandType type = CommandType.findCommandType(cmdWithParams[0]);

        if (type == null) {
            return new NotValidCommand(userId, content, sender);
        }

        switch (type) {
            case HELP:
                return new HelpCommand(userId, sender);
            case EXIT:
                return new ExitCommand(userId, closer);
            case RENAME:
                if (cmdWithParams.length < 2) {
                    throw new CommandParsingException("No new name specified");
                } else {
                    return new RenameCommand(userId, cmdWithParams[1], userStorage, sender);
                }
            case USERS:
                return new UsersCommand(userId, userStorage, sender);
            default:
                return new NotValidCommand(userId, content, sender);
        }
    }
}
