package org.artb.chat.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.RandomUtils;
import org.artb.chat.client.core.transport.ClientProcessor;
import org.artb.chat.client.core.transport.tcpnio.TcpNioClientProcessor;
import org.artb.chat.common.transport.tcpnio.TempDataStorage;
import org.artb.chat.common.Utils;
import org.artb.chat.common.configs.BotConfig;
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
    private final static int SMALL_WAITING_MS = 50;

    private final BotConfig config;
    private volatile ZonedDateTime startTime;

    private final TempDataStorage tempStorage = new TempDataStorage();

    public ChatBot(long botId, BotConfig config) {
        this.botName = "bot-" + botId;
        this.config = config;
        this.processor = new TcpNioClientProcessor(
                config.getHost(), config.getPort(), this::receive, this::disconnect);
    }

    private void disconnect() {
        if (running) {
            running = false;
        }
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
                TimeUnit.MILLISECONDS.sleep(SMALL_WAITING_MS);
            } catch (InterruptedException ignored) {
            }
        }

        for (int i = 0; i < config.getMsgCount() && running; i++) {
            try {
                LOGGER.info("sent {}, received {}", sentMsgCount.get(), receivedMsgCount.get());

                int waitMs = RandomUtils.nextInt(config.getMinWaitMs(), config.getMaxWaitMs() + 1);
                TimeUnit.MILLISECONDS.sleep(waitMs);

                Message msg;
                if (!authenticated) {
                    msg = MessageFactory.newAuthMessage(botName);
                } else {
                    msg = MessageFactory.newUserMessage(generateMsgContent(), botName);
                }

                String jsonMsg = Utils.serialize(msg);
                processor.acceptData(jsonMsg);

                // wait for success auth to prevent sending too much auth messages
//                while (running && !authenticated) {
//                    TimeUnit.MILLISECONDS.sleep(SMALL_WAITING_MS);
//                }

                sentMsgCount.incrementAndGet();
            } catch (InterruptedException ignored) {
            } catch (JsonProcessingException e) {
                LOGGER.warn("Cannot serialize msg", e.getMessage());
            }
        }
    }

    private void receive(String receivedData) {
        tempStorage.extractNextData(receivedData)
                .ifPresent((data) -> {
                    try {
                        Utils.deserializeMessageList(data).stream()
                                .filter(msg -> msg.getCreated().isAfter(startTime)) // skipping history
                                .forEach(msg -> {
                                    switch (msg.getType()) {
                                        case SUCCESS_AUTH:
                                            authenticated = true;
                                        case COMMAND_LIST:
                                        case USER_LIST:
                                            receivedMsgCount.incrementAndGet();
                                            break;
                                        case USER_TEXT:
                                            if (Objects.equals(msg.getSender(), botName)) {
                                                receivedMsgCount.incrementAndGet();
                                            }
                                    }
                                });
                    } catch (IOException e) {
                        LOGGER.warn("Cannot deserialize msg {}", e.getMessage(), data);
                    }
                });
    }


    public void stop() {
        if (running) {
            running = false;
            processor.stop();
        }
    }

    private String generateMsgContent() {
        List<String> commands = Arrays.asList("/help", "/users");
        int choice = RandomUtils.nextInt(0, 15);

        if (choice < commands.size()) {
            return commands.get(choice);
        }
        return randomAlphabetic(MIN_MSG_CONTENT_LEN, MAX_MSG_CONTENT_LEN + 1);
    }

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