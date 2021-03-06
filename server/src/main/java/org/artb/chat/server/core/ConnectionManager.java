package org.artb.chat.server.core;

import org.artb.chat.common.message.MessageType;
import org.artb.chat.server.core.event.ConnectionEvent;
import org.artb.chat.server.core.message.MessageSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

import static org.artb.chat.common.message.MessageFactory.newServerMessage;
import static org.artb.chat.common.message.MessageConstants.LEFT_CHAT_TEMPLATE;
import static org.artb.chat.common.message.MessageConstants.REQUEST_NAME_TEXT;

/**
 * Manage users connected and disconnected to the chat.
 * Here "Connection" means not a low-level connection, but a fact that a user is in the chat.
 */
public class ConnectionManager implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private final BlockingQueue<ConnectionEvent> events;
    private final MessageSender sender;
    private final AuthUserStorage storage;

    private volatile boolean running;

    public ConnectionManager(BlockingQueue<ConnectionEvent> events,
                             MessageSender sender,
                             AuthUserStorage storage) {
        this.events = events;
        this.sender = sender;
        this.storage = storage;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            final ConnectionEvent event;
            try {
                event = events.take();
            } catch (InterruptedException e) {
                LOGGER.error("Cannot take a message from queue", e);
                return;
            }

            switch (event.getType()) {
                case CONNECTED:
                    sender.sendPersonal(
                            event.getClientId(),
                            newServerMessage(REQUEST_NAME_TEXT, MessageType.NEED_AUTH));
                    break;
                case DISCONNECTED:
                    if (storage.authenticated(event.getClientId())) {
                        String user = storage.removeUser(event.getClientId());
                        String text = String.format(LEFT_CHAT_TEMPLATE, user);
                        sender.sendBroadcast(newServerMessage(text));
                    }
                    break;
            }
        }
    }

    public void stop() {
        running = false;
    }
}
