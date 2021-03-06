package org.artb.chat.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.artb.chat.client.core.transport.ClientProcessor;
import org.artb.chat.client.core.transport.tcpnio.TcpNioClientProcessor;
import org.artb.chat.common.Utils;
import org.artb.chat.common.configs.Config;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.message.MessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class ChatBot implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatBot.class);

    private final String botName;
    private final ClientProcessor processor;

    private final AtomicInteger receivedMsgCount = new AtomicInteger();
    private final AtomicInteger sentMsgCount = new AtomicInteger();

    private volatile boolean running;
    private volatile boolean authenticated;

    private final static int MIN_MSG_CONTENT_LEN = 0;
    private final static int MAX_MSG_CONTENT_LEN = 30;
    private final static int WAIT_AFTER_FINISH_MS = 5000;

    private final int waitMs;
    private final int msgCount;

    private final Random random = new Random();
    private volatile ZonedDateTime startTime;

    public ChatBot(Long botId, Config config, int msgCount, int waitMs) {
        this.botName = "bot-" + botId;
        this.msgCount = msgCount;
        this.waitMs = waitMs;
        this.processor = new TcpNioClientProcessor(
                config.getHost(), config.getPort(), this::receive, this::disconnect);
    }

    @Override
    public void run() {
        LOGGER.info("Starting {}", botName);
        running = true;
        startTime = ZonedDateTime.now();

        Thread processorThread = new Thread(processor::start, "processor-" + botName);
        processorThread.start();

        while (!processor.isRunning() && running) {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }

        for (int i = 0; i < msgCount && running; i++) {
            try {
                LOGGER.info("sent {}, received {}", sentMsgCount.get(), receivedMsgCount.get());
                TimeUnit.MILLISECONDS.sleep(waitMs + random.nextInt(50));

                Message msg;
                if (!authenticated) {
                    msg = MessageFactory.newAuthMessage(botName);
                } else {
                    msg = MessageFactory.newUserMessage(randomAlphabetic(MIN_MSG_CONTENT_LEN, MAX_MSG_CONTENT_LEN + 1), botName);
                }

                String jsonMsg = Utils.serialize(msg);
                processor.acceptData(jsonMsg);

                sentMsgCount.incrementAndGet();
            } catch (InterruptedException ignored) {
            } catch (JsonProcessingException e) {
                LOGGER.warn("Cannot serialize msg", e.getMessage());
            }
        }

        try {
            TimeUnit.MILLISECONDS.sleep(WAIT_AFTER_FINISH_MS);
        } catch (InterruptedException ignored) {
        } finally {
            processor.stop();
        }
    }

    private void receive(String data) {
        try {
            List<Message> messages = Utils.deserializeMessageList(data);
            messages.stream()
                    .filter(msg -> msg.getCreated().isAfter(startTime)) // skipping history
                    .forEach(msg -> {
                        switch (msg.getType()) {
                            case SUCCESS_AUTH:
                                authenticated = true;
                                receivedMsgCount.incrementAndGet();
                                break;
                            case COMMAND_LIST:
                            case USER_LIST:
                            case USER_TEXT:
                                if (Objects.equals(msg.getSender(), botName)) {
                                    receivedMsgCount.incrementAndGet();
                                }
                        }
                    });
        } catch (IOException e) {
            LOGGER.warn("Cannot deserialize msg {}", e.getMessage(), data);
        }
    }

    private void disconnect() {
        if (running) {
            running = false;
        }
    }

//    private String generateMsgContent() {
//        List<String> commands = Arrays.asList("/help", "/users");
//        int choice = random.nextInt(15);
//
//        if (choice < commands.size()) {
//            return commands.get(choice);
//        }
//        return randomAlphabetic(MIN_MSG_CONTENT_LEN, MAX_MSG_CONTENT_LEN + 1);
//    }

    public int getReceivedMessageCount() {
        return receivedMsgCount.get();
    }

    public int getSentMessagesCount() {
        return sentMsgCount.get();
    }

    public String getBotName() {
        return botName;
    }
}