package org.artb.chat.bot;

import org.artb.chat.common.Utils;
import org.artb.chat.common.configs.BotConfig;
import org.artb.chat.common.configs.SettingsParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class BotRunner {
    private final static Logger LOGGER = LoggerFactory.getLogger(BotRunner.class);

    public static void main(String[] args)  {
        try {
            BotConfig config = Utils.parseFromArgsArray(args, BotConfig.class);
            LOGGER.info("Bot config: {}", config);
            BotManager manager = new BotManager(config);
            manager.start();
        } catch (SettingsParseException e) {
            LOGGER.error("Cannot parse arguments: {}", Arrays.toString(args), e);
        } catch (Exception e) {
            LOGGER.error("Fatal client error", e);
        }
    }
}
