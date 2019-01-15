package org.artb.chat.server.core.command;

import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.event.ConnectionEvent;
import org.artb.chat.server.core.event.ConnectionEventType;
import org.artb.chat.server.core.message.MessageSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Consumer;


public class ExitCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExitCommand.class);

    private final UUID userId;
    private final Consumer<UUID> closer;

    public ExitCommand(UUID userId, Consumer<UUID> closer) {
        this.userId = userId;
        this.closer = closer;
    }

    @Override
    public void execute() {
        closer.accept(userId);
    }
}
