package org.artb.chat.server.core.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoNothingCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(DoNothingCommand.class);

    @Override
    public void execute() {
        LOGGER.info("Do nothing useful.");
    }
}
