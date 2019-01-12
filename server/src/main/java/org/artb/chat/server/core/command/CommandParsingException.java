package org.artb.chat.server.core.command;

public class CommandParsingException extends RuntimeException {

    public CommandParsingException(String info) {
        super(info);
    }
}
