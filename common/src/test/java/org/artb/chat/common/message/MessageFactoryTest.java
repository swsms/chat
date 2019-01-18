package org.artb.chat.common.message;

import org.testng.annotations.Test;

import static org.artb.chat.common.message.MessageFactory.NOT_IMPORTANT_SENDER;
import static org.artb.chat.common.message.MessageFactory.SERVER_SENDER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class MessageFactoryTest {

    @Test
    public void testNewUserMessage() {
        String msgContent = "123";

        Message msg = MessageFactory.newUserMessage(msgContent);

        assertEquals(msg.getContent(), msgContent);
        assertEquals(msg.getSender(), NOT_IMPORTANT_SENDER);
        assertEquals(msg.getType(), MessageType.USER_TEXT);
        assertNotNull(msg.getCreated());
    }

    @Test
    public void testNewUserMessageWithSender() {
        String msgContent = "abc";
        String sender = "123";

        Message msg = MessageFactory.newUserMessage(msgContent, sender);

        assertEquals(msg.getContent(), msgContent);
        assertEquals(msg.getSender(), sender);
        assertEquals(msg.getType(), MessageType.USER_TEXT);
        assertNotNull(msg.getCreated());
    }

    @Test
    public void testNewAuthMessage() {
        String msgContent = "auth";

        Message msg = MessageFactory.newAuthMessage(msgContent);

        assertEquals(msg.getContent(), msgContent);
        assertEquals(msg.getSender(), NOT_IMPORTANT_SENDER);
        assertEquals(msg.getType(), MessageType.TRY_AUTH);
        assertNotNull(msg.getCreated());
    }

    @Test
    public void testNewServerMessage() {
        String msgContent = "msg";

        Message msg = MessageFactory.newServerMessage(msgContent);

        assertEquals(msg.getContent(), msgContent);
        assertEquals(msg.getSender(), SERVER_SENDER);
        assertEquals(msg.getType(), MessageType.SERVER_TEXT);
        assertNotNull(msg.getCreated());
    }
}
