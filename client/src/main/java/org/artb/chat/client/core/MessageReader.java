package org.artb.chat.client.core;

import org.artb.chat.common.message.Message;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class MessageReader implements Runnable {

    private final Scanner scanner;
    private final Consumer<Message> consumer;
    private final AtomicBoolean runningFlag;

    public MessageReader(
            Consumer<Message> consumer,
            InputStream input,
            AtomicBoolean runningFlag) {
        this.consumer = consumer;
        this.runningFlag = runningFlag;
        this.scanner = new Scanner(input);
    }

    @Override
    public void run() {
        while (runningFlag.get() && scanner.hasNextLine()) {
            String messageText = scanner.nextLine();
            Message msg = Message.newUserMessage(messageText);
            consumer.accept(msg);
        }
    }
}
