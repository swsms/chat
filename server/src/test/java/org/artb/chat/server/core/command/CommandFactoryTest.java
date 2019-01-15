package org.artb.chat.server.core.command;

import org.artb.chat.server.core.message.MessageSender;
import org.artb.chat.server.core.storage.auth.AuthUserStorage;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class CommandFactoryTest {

    @Mock
    private MessageSender sender;

    @Mock
    private AuthUserStorage users;

    private final UUID userId = UUID.randomUUID();
    private CommandFactory factory;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        factory = new CommandFactory(sender, users, (UUID) -> { });
    }

    @DataProvider(name = "commands-provider")
    public static Object[][] users() {
        return new Object[][] {
                { "/help", HelpCommand.class },
                { "/exit", ExitCommand.class },
                { "/users", UsersCommand.class },
                { "/unknown", NotValidCommand.class },
                { "/rename anon", RenameCommand.class }
        };
    }

    @Test(dataProvider = "commands-provider")
    public void testCreateCommand(String cmd, Class<?> clazz) throws CommandParsingException {
        Command command = factory.createCommandForUser(userId, cmd);
        assertEquals(command.getClass(), clazz);
    }

    @Test(expectedExceptions = CommandParsingException.class)
    public void testCreateRenameCommandWithoutArgs() throws CommandParsingException {
        factory.createCommandForUser(userId, "/rename");
    }
}
