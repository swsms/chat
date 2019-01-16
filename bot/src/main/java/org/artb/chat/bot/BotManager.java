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

    public BotManager(Config config, int botsCount, int msgCount, int waitMs) {
        bots = generateBots(config, botsCount, msgCount, waitMs);
    }

    private List<ChatBot> generateBots(Config config, int botsCount, int msgCount, int waitMs) {
        return Stream
                .iterate(1L, id -> id + 1)
                .map(id -> new ChatBot(id, config, msgCount, waitMs))
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

        bots.forEach(bot -> LOGGER.info("{}: sent {}, received {}",
                bot.getBotName(), bot.getSentMessagesCount(), bot.getReceivedMessageCount()));

        int loss = bots.stream()
                .mapToInt((bot) -> bot.getSentMessagesCount() - bot.getReceivedMessageCount())
                .sum();

        LOGGER.info("===== Total loss: {} =====", loss);
    }
}
