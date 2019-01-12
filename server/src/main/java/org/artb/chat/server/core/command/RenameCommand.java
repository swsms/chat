package org.artb.chat.server.core.command;

// TODO this command needs sync
public class RenameCommand implements Command {

    private final String newName;

    public RenameCommand(String newName) {
        this.newName = newName;
    }

    @Override
    public void execute() {

    }
}
