package org.artb.chat.bot;

import org.artb.chat.common.Utils;
import org.artb.chat.common.configs.Config;
import org.artb.chat.common.configs.ConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class BotRunner {
    private final static Logger LOGGER = LoggerFactory.getLogger(BotRunner.class);

    private final static int BOTS_COUNT = 50;
    private final static int MSG_COUNT = 50;
    private final static int WAIT_MS = 500;

    public static void main(String[] args)  {
        try {
            Config config = Utils.parseFromArgsArray(args, Config.class);
            BotManager manager = new BotManager(config, BOTS_COUNT, MSG_COUNT, WAIT_MS);
            manager.start();
        } catch (ConfigParseException e) {
            LOGGER.error("Cannot parse arguments: {}", Arrays.toString(args), e);
        } catch (Exception e) {
            LOGGER.error("Fatal client error", e);
        }
    }
}
