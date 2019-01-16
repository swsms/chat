package org.artb.chat.client.core;

import org.artb.chat.client.core.transport.ClientProcessor;
import org.artb.chat.client.core.transport.tcpnio.TcpNioClientProcessor;
import org.artb.chat.client.ui.UIConsoleDisplay;
import org.artb.chat.client.ui.UIDisplay;
import org.artb.chat.common.Lifecycle;
import org.artb.chat.common.Utils;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.configs.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatClient implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);

    private final UIDisplay display = new UIConsoleDisplay();
    private final MessageReader reader;
    private final MessageHandler handler;
    private final AtomicBoolean authenticated = new AtomicBoolean();

    private final ClientProcessor processor;
    private final BlockingQueue<String> receivedDataQueue = new LinkedBlockingQueue<>();

    private volatile boolean running = false;

    public ChatClient(Config config, InputStream input) {
        this.reader = new MessageReader(this::send, input, authenticated);
        this.handler = new MessageHandler(display, receivedDataQueue, authenticated);

        this.processor = new TcpNioClientProcessor(config.getHost(), config.getPort());
        this.processor.setReceivedDataListener(receivedDataQueue::add);
        this.processor.setDisconnectHandler(() -> {
            this.stop();
            display.print("Disconnected from the server. Press any key to exit the client program.");
        });
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
        if (!running) {
            running = true;

            Thread processorThread = new Thread(processor::start, "processor");
            processorThread.start();

            Thread readerThread = new Thread(reader, "msg-reader");
            readerThread.start();

            Thread handlerThread = new Thread(handler, "msg-handler");
            handlerThread.start();
        } else {
            LOGGER.warn("Client has bee already started");
        }
    }

    @Override
    public void stop() {
        if (running) {
            running = false;
            reader.stop();
            handler.stop();
        }
    }
}
