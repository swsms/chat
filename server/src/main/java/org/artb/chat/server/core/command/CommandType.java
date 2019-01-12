package org.artb.chat.server.core.command;

public enum CommandType {
    HELP("/help", "show the list of commands"),
    EXIT("/exit", "leave the chat"),
    RENAME("/rename", "change your name to a new one"),
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
}
