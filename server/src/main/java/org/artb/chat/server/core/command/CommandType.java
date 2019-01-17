package org.artb.chat.server.core.command;

public enum CommandType {
    HELP("/help", "show the list of commands"),
    EXIT("/exit", "leave the chat"),
    RENAME("/rename", "change your name to a new one (e.g. /rename John), the name may contain spaces."),
    USERS("/users", "show all connected users");

    CommandType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private final String value;
    private final String desc;

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static CommandType findCommandType(String command) {
        CommandType[] types = CommandType.values();

        CommandType foundType = null;
        for (CommandType type : types) {
            if (command.equalsIgnoreCase(type.getValue())) {
                foundType = type;
                break;
            }
        }

        return foundType;
    }
}
