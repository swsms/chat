package org.artb.chat.client.core;

import org.artb.chat.client.ui.UIDisplay;
import org.artb.chat.common.Utils;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
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
                return;
            }
            try {
                if (receivedData != null) {
                    List<Message> messages = Utils.deserializeList(receivedData);
                    messages.forEach(this::handle);
                }
            } catch (IOException e) {
                LOGGER.error("Cannot deserialize data {}", receivedData, e);
            }
        }
    }

    private void handle(Message msg) {
        switch (msg.getType()) {
            case SUCCESS_AUTH:
                authenticated.set(true);
                display.print(msg.getContent());
                break;
            case USER_LIST:
                List<String> users = Utils.parseList(msg.getContent());
                display.printUserList(users);
                break;
            case USER_TEXT:
                LocalDateTime when = Utils.toLocalTimeZoneWithoutNano(msg.getCreated());
                display.printUserText(when, msg.getSender(), msg.getContent());
                break;
            default:
                display.print(msg.getContent());
        }
    }

    public void stop() {
        running = false;
    }
}
