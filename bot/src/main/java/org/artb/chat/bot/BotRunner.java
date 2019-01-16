package org.artb.chat.bot;

import org.artb.chat.common.Utils;
import org.artb.chat.common.configs.Config;
import org.artb.chat.common.configs.SettingsParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class BotRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotRunner.class);

    public static void main(String[] args)  {
        try {
            Config config = Utils.parseFromArgsArray(args, Config.class);
            BotManager manager = new BotManager(config, 2);
            manager.start();
        } catch (SettingsParseException e) {
            LOGGER.error("Cannot parse arguments: {}", Arrays.toString(args), e);
        } catch (Exception e) {
            LOGGER.error("Fatal client error", e);
        }
    }
}
