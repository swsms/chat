package org.artb.chat.common.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.artb.chat.common.Utils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class UtilsTest {

    @Test
    public void testSerialize() throws JsonProcessingException {
        Message msg = new Message();
        msg.setContent("Hello");
        Utils.serialize(msg);
    }

    @Test
    public void testDeserialize() throws IOException {
        String json = "{\"content\":\"Hello\"}";
        Message msg = Utils.deserialize(json);
        assertEquals(msg.getContent(), "Hello");
    }

    @Test
    public void testDeserializeList() throws IOException {
        String json =
                "[{\"content\":\"The name was accepted.\",\"type\":\"SERVER_TEXT\",\"sender\":\"server\"}, " +
                "{\"content\":\"The name was accepted.\",\"type\":\"SERVER_TEXT\",\"sender\":\"server\"}]";
        List<Message> messageList = Utils.deserializeList(json);
        assertEquals(messageList.size(), 2);
    }

    @Test
    public void testCreateBatch() throws IOException {
        List<String> jsons = Arrays.asList(
                "{\"content\":\"Hello\"}",
                "{\"content\":\"Hi\"}",
                "{\"content\":\"Cia\"}");

        String jsonArray = Utils.createBatch(jsons);
        List<Message> messages = Utils.deserializeList(jsonArray);

        assertEquals(messages.size(), 3);
    }

    @Test
    public void testIsBlank() {
        String emptyString = "";
        assertTrue(Utils.isBlank(emptyString));

        String nullString = null;
        assertTrue(Utils.isBlank(nullString));

        String stringWithSpaces = " \n\r";
        assertTrue(Utils.isBlank(stringWithSpaces));
    }
}
