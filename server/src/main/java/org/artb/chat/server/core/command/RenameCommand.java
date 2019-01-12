package org.artb.chat.server.core.command;

public class RenameCommand implements Command {

    private final String newName;

    public RenameCommand(String newName) {
        this.newName = newName;
    }

    @Override
    public void execute() {

    }
}
