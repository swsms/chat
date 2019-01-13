package org.artb.chat.client.core;

import org.artb.chat.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class MessageReader implements Runnable {

    private final Scanner scanner = new Scanner(System.in);
    private final Consumer<Message> consumer;
    private final AtomicBoolean runningFlag;

    public MessageReader(Consumer<Message> consumer, AtomicBoolean runningFlag) {
        this.consumer = consumer;
        this.runningFlag = runningFlag;
    }

    @Override
    public void run() {
        while (runningFlag.get()) {
            String messageText = scanner.nextLine();
            Message msg = Message.newUserMessage(messageText);
            consumer.accept(msg);
        }
    }
}
