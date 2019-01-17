package org.artb.chat.client;

import org.artb.chat.client.core.ChatClient;
import org.artb.chat.common.Utils;
import org.artb.chat.common.configs.Config;
import org.artb.chat.common.configs.ConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


public class ClientRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRunner.class);

    public static void main(String[] args) {
        try {
            Config config = Utils.parseFromArgsArray(args, Config.class);
            ChatClient client = new ChatClient(config);
            client.start();
        } catch (ConfigParseException e) {
            LOGGER.error("Cannot parse arguments: {}", Arrays.toString(args), e);
        } catch (Exception e) {
            LOGGER.error("Fatal client error", e);
        }
    }
}