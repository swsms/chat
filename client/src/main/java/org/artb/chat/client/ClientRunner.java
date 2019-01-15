package org.artb.chat.client;

import org.artb.chat.client.core.ChatClient;
import org.artb.chat.common.Utils;
import org.artb.chat.common.configs.Config;
import org.artb.chat.common.configs.SettingsParseException;
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
//            runBot(settings);
        } catch (SettingsParseException e) {
            LOGGER.error("Cannot parse arguments: {}", Arrays.toString(args), e);
        } catch (Exception e) {
            LOGGER.error("Fatal client error", e);
        }
    }

//    private static void runBot(Settings settings) {
//        String initialString = "JOHN\nHello!\nhow are u today?\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n" +
//                "Hello!\nhow are u today?\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10" +
//                "Hello!\nhow are u today?\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10";
//        InputStream clientInput = new ByteArrayInputStream(initialString.getBytes());
//        ChatClient client = new TcpNioChatClient(settings.getHost(), settings.getPort(), clientInput);
//        client.start();
//    }
}