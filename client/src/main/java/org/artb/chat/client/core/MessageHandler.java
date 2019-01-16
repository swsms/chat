package org.artb.chat.client.core;

import org.artb.chat.client.ui.UIDisplay;
import org.artb.chat.common.Utils;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageHandler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);

    private volatile boolean running;

    private final UIDisplay display;
    private final BlockingQueue<String> dataQueue;
    private final long TIMEOUT_MILLIS = 200;

    private AtomicBoolean authenticated;

    public MessageHandler(UIDisplay display,
                          BlockingQueue<String> dataQueue,
                          AtomicBoolean authenticated) {
        this.display = display;
        this.dataQueue = dataQueue;
        this.authenticated = authenticated;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            final String receivedData;
            try {
                receivedData = dataQueue.poll(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Cannot take a message from queue", e);
                return;
            }
            try {
                if (receivedData != null) {
                    List<Message> messages = Utils.deserializeList(receivedData);
                    messages.forEach(msg -> {
                        if (msg.getType() == MessageType.SUCCESS_AUTH) {
                            authenticated.set(true);
                        }
                        display.print(msg);
                    });
                }
            } catch (IOException e) {
                LOGGER.error("Cannot deserialize data {}", receivedData, e);
            }
        }
    }

    public void stop() {
        running = false;
    }
}
