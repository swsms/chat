package org.artb.chat.server.core.storage.history;

import org.artb.chat.common.message.Message;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * Thread-safe history storage
 */
public class HistoryStorage {

    private final ConcurrentLinkedQueue<Message> history = new ConcurrentLinkedQueue<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final int size;

    public HistoryStorage(int size) {
        if (size < 1) {
            throw new IllegalArgumentException(
                    "Illegal history size: " + size);
        }
        this.size = size;
    }

    public void add(Message msg) {
        lock.writeLock().lock();
        try {
            while(history.size() >= size) {
                history.poll();
            }
            history.add(msg);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Message> history() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(history);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void forEach(Consumer<Message> consumer) {
        history.forEach(consumer);
    }
}
