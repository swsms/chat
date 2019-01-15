package org.artb.chat.client.core;

import org.artb.chat.client.core.processor.ClientProcessor;
import org.artb.chat.client.core.processor.tcpnio.TcpNioClientProcessor;
import org.artb.chat.client.ui.UIConsoleDisplay;
import org.artb.chat.client.ui.UIDisplay;
import org.artb.chat.common.Lifecycle;
import org.artb.chat.common.Utils;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.settings.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ChatClient implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);

    private final ClientProcessor processor;

    private final UIDisplay display = new UIConsoleDisplay();
    private final MessageReader reader;
    // TODO private final MessageProcessor

    public ChatClient(Config config, InputStream input) {
        this.processor = new TcpNioClientProcessor(config.getHost(), config.getPort());
        this.reader = new MessageReader(this::send, input);
        prepareProcessor();
    }

    public ChatClient(Config config) {
        this(config, System.in);
    }

    private void send(Message msg) {
        try {
            String data = Utils.serialize(msg);
            processor.acceptData(data);
        } catch (IOException e) {
            LOGGER.error("Cannot serialize msg: {}", msg, e);
        }
    }

    @Override
    public void start() {
        Thread processorThread = new Thread(processor::start, "processor");
        processorThread.start();

        Thread readerThread = new Thread(reader, "msg-reader");
        readerThread.start();
    }

    private void prepareProcessor() {
        processor.setDisconnectHandler(() -> {
            this.stop();
            display.print("Disconnected from the server. Press any key to exit the client program.");
        });
        processor.setReceivedDataListener((data) -> {
            try {
                List<Message> messages = Utils.deserializeList(data);
                messages.forEach(display::print);
            } catch (IOException e) {
                LOGGER.error("Cannot deserialize data {}", data, e);
            }
        });
    }

    @Override
    public void stop() {
        reader.stop();
    }
}
