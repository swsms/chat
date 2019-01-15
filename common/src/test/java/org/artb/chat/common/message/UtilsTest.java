package org.artb.chat.common.message;

import org.artb.chat.common.Utils;
import org.artb.chat.common.settings.Config;
import org.artb.chat.common.settings.ServerConfig;
import org.artb.chat.common.settings.SettingsParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class UtilsTest {

    @Test
    public void testParseFromArgsArrayWhenEmpty() throws SettingsParseException {
        String[] args = { };
        Config settings = Utils.parseFromArgsArray(args,  Config.class);
        Assert.assertEquals(settings.getHost(), "localhost");
        Assert.assertEquals(settings.getPort(), 8999);
    }

    @Test
    public void testParseFromArgsArrayWhenNonEmpty() throws SettingsParseException {
        String[] args = { "--host", "127.0.0.1", "--port", "9999" };
        Config settings = Utils.parseFromArgsArray(args, Config.class);
        Assert.assertEquals(settings.getHost(), "127.0.0.1");
        Assert.assertEquals(settings.getPort(), 9999);
    }

    @Test
    public void testParseFromArgsArrayForChildClass() throws SettingsParseException {
        String[] args = { "--host", "127.0.0.1", "--port", "9999", "--msg-processors", "4" };
        ServerConfig settings = Utils.parseFromArgsArray(args,  ServerConfig.class);
        Assert.assertEquals(settings.getHost(), "127.0.0.1");
        Assert.assertEquals(settings.getPort(), 9999);
        Assert.assertEquals(settings.getMsgProcessors(), 4);
    }

    @Test
    public void testSerialize() throws IOException {
        Message msg = new Message();
        msg.setContent("Hello");
        String serializedMsg = Utils.serialize(msg);
        assertNotNull(serializedMsg);
        Utils.deserialize(serializedMsg);
    }

    @Test
    public void testDeserializeWithServed() throws IOException {
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

    @Test
    public void testCreateNewListWithMessage() {
        List<Integer> famousNumbers = Arrays.asList(1, 1, 2, 3, 5, 8);
        List<Integer> newFamousNumbers = Utils.createNewListWithMessage(0, famousNumbers);
        assertEquals(newFamousNumbers.size(), famousNumbers.size() + 1);
        assertEquals((int) newFamousNumbers.get(0), 0);
    }
}
