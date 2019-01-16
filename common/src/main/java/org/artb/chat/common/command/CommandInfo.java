package org.artb.chat.common.command;

/**
 * The class is used to isolate client code from server commands.
 * It allows us to add new command without changing the client.
 */
public class CommandInfo {
    private String command;
    private String description;

    public CommandInfo() { }

    public CommandInfo(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
