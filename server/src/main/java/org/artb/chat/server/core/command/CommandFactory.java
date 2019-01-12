package org.artb.chat.server.core.command;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.server.core.message.MsgSender;

public class CommandFactory {

    private final BufferedConnection connection;
    private final MsgSender sender;

    public CommandFactory(BufferedConnection connection, MsgSender sender) {
        this.connection = connection;
        this.sender = sender;
    }

    /**
     * It creates a command according to the given line.
     *
     * @param content the string representation of a command (with parameters)
     *
     * @return command or a special type DoNothingCommand
     */
    public Command createCommand(String content) {
        String[] lineParts = content.split("\\s+");
        String command = lineParts[0];

        CommandType[] types = CommandType.values();

        CommandType foundCommand = null;
        for (CommandType type : types) {
            if (command.equalsIgnoreCase(type.getValue())) {
                foundCommand = type;
                break;
            }
        }

        if (foundCommand == null) {
            return new NotValidCommand(content, connection, sender);
        }

        switch (foundCommand) {
            case HELP:
                return new HelpCommand();
            case EXIT:
                return new ExitCommand(connection, sender);
            case RENAME:
                if (lineParts.length < 2) {
                    throw new CommandParsingException("No new name specified");
                } else {
                    return new RenameCommand(lineParts[1]);
                }
            case USERS:
                return new UsersCommand();
            default:
                return new NotValidCommand(content, connection, sender);
        }
    }
}
