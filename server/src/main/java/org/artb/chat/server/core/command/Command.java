package org.artb.chat.server.core.command;

public interface Command {
    /**
     * All messages that start with the "/" character are interpreted as commands like in Slack.
     */
    String CMD_CHAR = "/";

    void execute();
}
