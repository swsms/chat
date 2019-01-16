package org.artb.chat.bot;

import org.artb.chat.common.configs.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BotManager  {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotManager.class);

    private final List<ChatBot> bots;

    public BotManager(Config config, int botsCount) {
        bots = generateBots(config, botsCount);
    }

    private List<ChatBot> generateBots(Config config, int botsCount) {
        return Stream
                .iterate(1L, id -> id + 1)
                .map(id -> new ChatBot(id, config))
                .limit(botsCount)
                .collect(Collectors.toList());
    }

    public void start() {
        List<Thread> threads = bots.stream()
                .map(bot -> new Thread(bot, bot.getBotName()))
                .collect(Collectors.toList());

        threads.forEach(Thread::start);

        try {
            for (Thread botThread : threads) {
                botThread.join();
            }

        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }

        bots.forEach(bot -> LOGGER.info("sent {}, received {}", bot.getSentMessagesCount(), bot.getReceivedMessageCount()));
    }
}
