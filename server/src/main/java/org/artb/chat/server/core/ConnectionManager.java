package org.artb.chat.server.core;

import org.artb.chat.common.connection.BufferedConnection;
import org.artb.chat.common.message.Message;
import org.artb.chat.server.core.event.ConnectionEvent;
import org.artb.chat.server.core.message.MsgSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.artb.chat.server.core.message.MsgConstants.LEFT_CHAT_TEMPLATE;
import static org.artb.chat.server.core.message.MsgConstants.REQUEST_NAME_TEXT;

public class ConnectionManager implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private final BlockingQueue<ConnectionEvent> events;
    private final AtomicBoolean runningFlag;
    private final MsgSender sender;
    private final AuthUserStorage storage;

    public ConnectionManager(BlockingQueue<ConnectionEvent> events,
                             AtomicBoolean runningFlag,
                             MsgSender sender,
                             AuthUserStorage storage) {
        this.events = events;
        this.runningFlag = runningFlag;
        this.sender = sender;
        this.storage = storage;
    }

    @Override
    public void run() {
        while (runningFlag.get()) {
            final ConnectionEvent event;
            try {
                event = events.take();
            } catch (InterruptedException e) {
                LOGGER.error("Cannot take a message from queue", e);
                return;
            }

            switch (event.getType()) {
                case CONNECTED:
                    sender.sendPersonal(event.getClientId(), Message.newServerMessage(REQUEST_NAME_TEXT));
                    break;
                case DISCONNECTED:
                    if (storage.authenticated(event.getClientId())) {
                        String user = storage.removeUser(event.getClientId());
                        String text = String.format(LEFT_CHAT_TEMPLATE, user);
                        sender.sendBroadcast(Message.newServerMessage(text));
                    }
                    break;
            }
        }
    }
}
