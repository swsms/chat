package org.artb.chat.client.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.artb.chat.client.ui.UIDisplay;
import org.artb.chat.common.Utils;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.message.MessageFactory;
import org.artb.chat.common.message.MessageType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.verify;

public class MessageHandlerTest {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    private MessageHandler handler;

    @Mock
    private UIDisplay displayMock;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        handler = new MessageHandler(displayMock, queue, new AtomicBoolean());
    }

    @Test
    public void testHandleAuth() throws IOException, InterruptedException {
        new Thread(handler, "test-handler").start();

        Message msg = MessageFactory.newServerMessage("success auth", MessageType.SUCCESS_AUTH);
        queue.add(Utils.serializeList(Collections.singletonList(msg)));
        Thread.sleep(2000L);
        Assert.assertTrue(handler.isAuthenticated().get());

        verify(displayMock).print(msg.getContent());
    }
}
