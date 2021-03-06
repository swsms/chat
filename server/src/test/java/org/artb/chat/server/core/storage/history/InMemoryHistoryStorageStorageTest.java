package org.artb.chat.server.core.storage.history;

import org.artb.chat.common.Constants;
import org.artb.chat.common.message.Message;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artb.chat.common.message.MessageFactory.newServerMessage;
import static org.artb.chat.common.message.MessageFactory.newUserMessage;
import static org.testng.Assert.*;

public class InMemoryHistoryStorageStorageTest {

    @Test
    public void testAddInSingleElementQueue() {
        HistoryStorage storage = new InMemoryHistoryStorage(1);

        storage.add(newUserMessage("Hello"));

        List<Message> history = storage.history();
        assertEquals(history.size(), 1);
        assertEquals(history.get(0).getContent(), "Hello");

        storage.add(newUserMessage("Hi"));

        history = storage.history();
        assertEquals(history.size(), 1);
        assertEquals(history.get(0).getContent(), "Hi");
    }

    @Test
    public void testAddInLargeQueue() {
        HistoryStorage storage = new InMemoryHistoryStorage(Constants.HISTORY_SIZE);

        for (int i = 0; i < Constants.HISTORY_SIZE; i++) {
            storage.add(newUserMessage("Hello" + i));
        }

        List<Message> history = storage.history();
        assertEquals(history.size(), 100);
        assertEquals(history.get(0).getContent(), "Hello0");
        assertEquals(history.get(99).getContent(), "Hello99");

        storage.add(newUserMessage("Hi"));

        history = storage.history();
        assertEquals(history.size(), 100);
        assertEquals(history.get(0).getContent(), "Hello1");
        assertEquals(history.get(99).getContent(), "Hi");
    }

    @Test
    public void testStorageConcurrently() throws InterruptedException {
        HistoryStorage storage = new InMemoryHistoryStorage(Constants.HISTORY_SIZE);

        AtomicInteger counter = new AtomicInteger();

        List<Thread> workers = Stream.generate(() ->
                new Thread(() -> {
                    for (int i = 0; i < 100_000; i++) {
                        int num = counter.incrementAndGet();
                        storage.add(newServerMessage(Objects.toString(num)));
                    }
        })).limit(10).collect(Collectors.toList());

        workers.forEach(Thread::start);

        for (Thread t : workers) {
            t.join();
        }

        assertEquals(storage.history().size(), Constants.HISTORY_SIZE);
    }
}
