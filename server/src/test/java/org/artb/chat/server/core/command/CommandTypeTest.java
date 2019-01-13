package org.artb.chat.server.core.command;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class CommandTypeTest {

    @Test
    public void testFindCommandType() {
        assertNull(CommandType.findCommandType("/cccccc"));
        assertNotNull(CommandType.findCommandType("/help"));
    }
}
