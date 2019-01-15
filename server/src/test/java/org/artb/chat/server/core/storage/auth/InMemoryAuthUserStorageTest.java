package org.artb.chat.server.core.storage.auth;

import org.testng.annotations.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.*;

public class InMemoryAuthUserStorageTest {
    private AuthUserStorage storage = null;
    private Random rand = new Random();

    @BeforeMethod
    public void prepareStorage() {
        storage = new InMemoryAuthUserStorage();
    }

    @DataProvider(name = "users-provider")
    public static Object[][] users() {
        return new Object[][] {
                { UUID.randomUUID(), "John" },
                { UUID.randomUUID(), "Katie"},
                { UUID.randomUUID(), "unknown"}
        };
    }

    @Test(dataProvider = "users-provider")
    public void testUpsertWhenNoConflict(UUID userId, String userName) throws InvalidNameException {
        storage.upsertUserName(userId, userName);
        assertEquals(storage.getUserName(userId), userName);
    }

    @Test(expectedExceptions = { InvalidNameException.class} )
    public void testUpsertWhenNameConflict() throws InvalidNameException {
        storage.upsertUserName(UUID.randomUUID(), "Superuser");
        storage.upsertUserName(UUID.randomUUID(), "Superuser");
    }

    @Test(expectedExceptions = { InvalidNameException.class} )
    public void testUpsertWhenBlankName() throws InvalidNameException {
        storage.upsertUserName(UUID.randomUUID(), "    ");
    }

    @Test(expectedExceptions = { InvalidNameException.class} )
    public void testUpsertWhenNullName() throws InvalidNameException {
        storage.upsertUserName(UUID.randomUUID(), null);
    }

    @Test
    public void testUpsertUserNameConcurrently() throws InterruptedException {
        List<String> names = Arrays.asList("John", "Katie", "User", "unknown", "abc");

        int numberOfWorkers = 8;

        CountDownLatch latch = new CountDownLatch(numberOfWorkers);
        List<Thread> upsertWorkers = Stream
                .generate(() -> new Thread(generateWorker(names, latch)))
                .limit(numberOfWorkers)
                .collect(Collectors.toList());

        upsertWorkers.forEach(Thread::start);
        latch.await();
        for (Thread worker : upsertWorkers) {
            worker.join();
        }

        Map<UUID, String> users = storage.getUsers();
        assertTrue(users.size() <= names.size());

        Map<String, Long> nameToCount =
                users.values().stream()
                        .collect(Collectors.groupingBy(name -> name, Collectors.counting()));

        // Check whether no duplicate names
        nameToCount.forEach((name, count) -> assertEquals((long) count, 1));
    }

    private Runnable generateWorker(List<String> names, CountDownLatch latch) {
        return () -> {
            latch.countDown();
            for (int i = 0; i < 10_000; i++) {
                String name = names.get(rand.nextInt(names.size()));
                UUID id = UUID.randomUUID();
                try {
                    storage.upsertUserName(id, name);
                } catch (InvalidNameException ignored) {
                }
            }
        };
    }

}
