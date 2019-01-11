package org.artb.chat.client.core;

import org.artb.chat.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.function.Consumer;

public class AsyncMsgReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncMsgReader.class);

    private final Scanner scanner = new Scanner(System.in);
    private final Consumer<Message> consumer;
    private final Thread thread;
    private volatile boolean running = false;

    public AsyncMsgReader(Consumer<Message> consumer) {
        this.consumer = consumer;
        this.thread = new Thread(prepareRunnable());
    }

    private Runnable prepareRunnable() {
        return () -> {
            while (running) {
                String messageText = scanner.nextLine();
                Message msg = Message.newUserMessage(messageText);
                consumer.accept(msg);
            }
            LOGGER.info("Successfully stopped");
        };
    }

    public void start() {
        running = true;
        thread.start();
    }

    public void stop() {
        running = false;
    }
}
