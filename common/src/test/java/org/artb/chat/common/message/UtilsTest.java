package org.artb.chat.common.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.artb.chat.common.Utils;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class UtilsTest {

    @Test
    public void testSerialize() throws JsonProcessingException {
        Message msg = new Message();
        msg.setContent("Hello");
        Utils.serialize(msg);
    }

    @Test
    public void testDeserialization() throws IOException {
        String json = "{\"content\":\"Hello\"}";
        Message msg = Utils.deserialize(json);
        assertEquals(msg.getContent(), "Hello");
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
