package org.artb.chat.server;

import org.artb.chat.common.settings.Settings;
import org.artb.chat.common.settings.SettingsParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ServerRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRunner.class);

    public static void main(String[] args) {
        try {
            Settings settings = Settings.fromArgsArray(args);
            ChatServer server = new ChatServer(settings.getHost(), settings.getPort());
            Thread serverThread = new Thread(server::start, "main-chat-thread");
            serverThread.start();
        } catch (SettingsParseException e) {
            LOGGER.error("Cannot parse arguments: {}", Arrays.toString(args), e);
        } catch (Exception e) {
            LOGGER.error("Fatal server error", e);
        }
    }
}
