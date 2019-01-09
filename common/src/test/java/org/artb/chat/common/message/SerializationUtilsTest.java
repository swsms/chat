package org.artb.chat.common.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class SerializationUtilsTest {

    @Test
    public void testSerialize() throws JsonProcessingException {
        Message msg = new Message();
        msg.setContent("Hello");
        SerializationUtils.serialize(msg);
    }

    @Test
    public void testDeserialization() throws IOException {
        String json = "{\"content\":\"Hello\"}";
        Message msg = SerializationUtils.deserialize(json);
        assertEquals(msg.getContent(), "Hello");
    }
}
