package org.artb.chat.server.core.command;

public enum CommandType {
    HELP("/help", "Show a list of commands"),
    EXIT("/exit", "Leave the chat"),
    RENAME("/rename", "Change your name"),
    USERS("/users", "Get a list of connected users");

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
}
