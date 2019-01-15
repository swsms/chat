package org.artb.chat.client.core;

import org.artb.chat.common.message.Message;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class MessageReader implements Runnable {

    private final Scanner scanner;
    private final Consumer<Message> consumer;
    private volatile boolean running;

    public MessageReader(
            Consumer<Message> consumer,
            InputStream input) {
        this.consumer = consumer;
        this.scanner = new Scanner(input);
    }

    @Override
    public void run() {
        running = true;
        while (running && scanner.hasNextLine()) {
            String messageText = scanner.nextLine();
            Message msg = Message.newUserMessage(messageText);
            consumer.accept(msg);
        }
    }

    public void stop() {
        this.running = false;
    }
}
