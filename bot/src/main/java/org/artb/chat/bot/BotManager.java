package org.artb.chat.bot;

import org.artb.chat.common.configs.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BotManager  {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotManager.class);

    private final static int WAIT_AFTER_FINISH_MS = 15000;
    private final List<ChatBot> bots;

    public BotManager(BotConfig config) {
        bots = generateBots(config);
    }

    private List<ChatBot> generateBots(BotConfig config) {
        return Stream
                .iterate(1L, id -> id + 1)
                .map(id -> new ChatBot(id, config))
                .limit(config.getBotsCount())
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

            LOGGER.info("==== Wait {} ms before stopping all threads ====",  WAIT_AFTER_FINISH_MS);
            TimeUnit.MILLISECONDS.sleep(WAIT_AFTER_FINISH_MS);
            bots.forEach(ChatBot::stop);
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
