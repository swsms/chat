package org.artb.chat.server;

import org.artb.chat.common.Utils;
import org.artb.chat.common.configs.ServerConfig;
import org.artb.chat.common.configs.SettingsParseException;
import org.artb.chat.server.core.ChatServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ServerRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRunner.class);

    public static void main(String[] args) {
        try {
            ServerConfig config = Utils.parseFromArgsArray(args, ServerConfig.class);
            ChatServer chat = new ChatServer(config);
            chat.start();
        } catch (SettingsParseException e) {
            LOGGER.error("Cannot parse arguments: {}", Arrays.toString(args), e);
        } catch (Exception e) {
            LOGGER.error("Fatal server error", e);
        }
    }
}
