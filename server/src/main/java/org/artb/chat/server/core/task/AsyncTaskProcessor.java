package org.artb.chat.server.core.task;

import org.artb.chat.common.Utils;
import org.artb.chat.server.ChatServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class AsyncTaskProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTaskProcessor.class);

    private final ChatServer chat;
    private final BlockingQueue<SendingTask> tasksQueue;
    private final Thread thread;
    private volatile boolean running = false;

    public AsyncTaskProcessor(ChatServer chat, BlockingQueue<SendingTask> tasksQueue) {
        this.chat = chat;
        this.tasksQueue = tasksQueue;
        this.thread = new Thread(prepareRunnable());
    }

    private Runnable prepareRunnable() {
        return () -> {
            while (running) {
                try {
                    SendingTask task = tasksQueue.take();
                    LOGGER.info("Message to send: {}", task.getMessage());

                    String msgJson = Utils.serialize(task.getMessage());
                    switch (task.getMode()) {
                        case PERSONAL:
//                            chat.sendOne(task.getClientId(), msgJson);
                            break;
                        case BROADCAST:
//                            chat.sendBroadcast(msgJson);
                            break;
                    }
                } catch (Exception e) {
                    LOGGER.error("Cannot send message", e);
                }
            }
            LOGGER.info("Successfully stopped");
        };
    }

    public void start() {
        running = true;
        thread.start();
    }

    public void stop() {
        running = false;
    }
}
