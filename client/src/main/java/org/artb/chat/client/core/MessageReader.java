package org.artb.chat.client.core;

import org.artb.chat.common.message.Message;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.artb.chat.common.message.MessageFactory.newAuthMessage;
import static org.artb.chat.common.message.MessageFactory.newUserMessage;

public class MessageReader implements Runnable {

    private final Scanner scanner;
    private final Consumer<Message> consumer;
    private volatile boolean running;
    private AtomicBoolean authenticated;

    public MessageReader(
            Consumer<Message> consumer,
            InputStream input,
            AtomicBoolean authenticated) {
        this.consumer = consumer;
        this.scanner = new Scanner(input);
        this.authenticated = authenticated;
    }

    @Override
    public void run() {
        running = true;
        while (running && scanner.hasNextLine()) {
            String messageText = scanner.nextLine();
            Message msg = authenticated.get() ?
                    newUserMessage(messageText) :
                    newAuthMessage(messageText);
            consumer.accept(msg);
        }
    }

    public void stop() {
        this.running = false;
    }
}
