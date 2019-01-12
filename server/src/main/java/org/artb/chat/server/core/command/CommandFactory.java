package org.artb.chat.server.core.command;

public class CommandFactory {

    /**
     * It creates a command according to the given line.
     *
     * @param line the string representation of a command (with parameters)
     *
     * @return command or a special type DoNothingCommand
     */
    public Command createCommand(String line) throws CommandParsingException {
        String[] lineParts = line.split("\\s+");
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
            return new DoNothingCommand();
        }

        switch (foundCommand) {
            case HELP:
                return new HelpCommand();
            case EXIT:
                return new HelpCommand();
            case RENAME:
                if (lineParts.length < 2) {
                    throw new CommandParsingException("No new name specified");
                } else {
                    return new RenameCommand(lineParts[1]);
                }
            case USERS:
                return new UsersCommand();
            default:
                return new DoNothingCommand();
        }
    }
}
