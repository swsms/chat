package org.artb.chat.common.message;

import org.artb.chat.common.settings.Settings;
import org.artb.chat.common.settings.SettingsParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SettingsTest {

    @Test
    public void testFromEmptyArgsArray() throws SettingsParseException {
        String[] args = { };
        Settings settings = Settings.fromArgsArray(args);
        Assert.assertEquals(settings.getHost(), "localhost");
        Assert.assertEquals(settings.getPort(), 8999);
    }

    @Test
    public void testFromNonEmptyArgsArray() throws SettingsParseException {
        String[] args = { "-host", "127.0.0.1", "-port", "9999" };
        Settings settings = Settings.fromArgsArray(args);
        Assert.assertEquals(settings.getHost(), "127.0.0.1");
        Assert.assertEquals(settings.getPort(), 9999);
    }
}
