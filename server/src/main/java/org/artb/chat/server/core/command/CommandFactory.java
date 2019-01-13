package org.artb.chat.server.core.command;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.server.core.message.MsgSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;

public class CommandFactory {

    private final BufferedConnection connection;
    private final MsgSender sender;
    private final AuthUserStorage userStorage;

    public CommandFactory(BufferedConnection connection,
                          MsgSender sender,
                          AuthUserStorage userStorage) {

        this.connection = connection;
        this.sender = sender;
        this.userStorage = userStorage;
    }

    /**
     * It creates a command according to the given string content.
     *
     * @param content the string representation of a command including parameters
     *
     * @return command or a special type DoNothingCommand
     */
    public Command createCommand(String content) throws CommandParsingException {
        String[] commandWithParams = content.split("\\s+");

        CommandType type = CommandType.findCommandType(commandWithParams[0]);

        if (type == null) {
            return new NotValidCommand(content, connection, sender);
        }

        switch (type) {
            case HELP:
                return new HelpCommand(connection, sender);
            case EXIT:
                return new ExitCommand(userStorage, connection, sender);
            case RENAME:
                if (commandWithParams.length < 2) {
                    throw new CommandParsingException("No new name specified");
                } else {
                    return new RenameCommand(commandWithParams[1],
                            userStorage, connection, sender);
                }
            case USERS:
                return new UsersCommand(userStorage, connection, sender);
            default:
                return new NotValidCommand(content, connection, sender);
        }
    }
}
