package org.artb.chat.client.core;

import org.artb.chat.common.message.Message;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

public class MessageReaderTest {

    @Test
    public void testRun() throws InterruptedException {
        String inputData = "JOHN\nHello!\nhow are you today?\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n" +
                "Hello!\nhow are you today?\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10" +
                "Hello!\nhow are you today?\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10";

        InputStream clientInput = new ByteArrayInputStream(inputData.getBytes());
        AtomicBoolean runningFlag = new AtomicBoolean(true);
        ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();

        MessageReader reader = new MessageReader(queue::add, clientInput, runningFlag);
        Thread readerThread = new Thread(reader);
        readerThread.start();
        readerThread.join();

        List<String> inputDataParts = Arrays.asList(inputData.split("\\n+"));
        assertEquals(inputDataParts.size(), queue.size());

        List<String> readMessages = queue.stream().map(Message::getContent).collect(Collectors.toList());
        assertEquals(inputDataParts, readMessages);
    }

    private void consumeMessage(Message msg) {

    }
}
