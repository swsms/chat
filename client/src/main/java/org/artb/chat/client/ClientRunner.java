package org.artb.chat.client;

import org.artb.chat.client.core.ChatClient;
import org.artb.chat.client.core.tcpnio.TcpNioChatClient;
import org.artb.chat.common.settings.Settings;
import org.artb.chat.common.settings.SettingsParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


public class ClientRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRunner.class);

    public static void main(String[] args) {
        try {
            Settings settings = Settings.fromArgsArray(args);
            ChatClient client = new TcpNioChatClient(settings.getHost(), settings.getPort());
            client.start();
        } catch (SettingsParseException e) {
            LOGGER.error("Cannot parse arguments: {}", Arrays.toString(args), e);
        } catch (Exception e) {
            LOGGER.error("Fatal client error", e);
        }
    }
}