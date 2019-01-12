package org.artb.chat.server.core.command;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.message.MsgSender;

import java.io.IOException;

public class ExitCommand implements Command {
    private final BufferedConnection connection;
    private final MsgSender sender;

    public ExitCommand(BufferedConnection connection, MsgSender sender) {
        this.connection = connection;
        this.sender = sender;
    }

    @Override
    public void execute() {
        sender.send(
                connection.getId(),
                Message.newServerMessage("Disconnected from the server."));
        // TODO need accurate closing
        try {
            connection.close();
        } catch (IOException e) {
            System.out.println("Cannot close");
        }
    }
}
