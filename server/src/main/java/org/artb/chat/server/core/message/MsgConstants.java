package org.artb.chat.server.core.message;

public final class MsgConstants {

    private MsgConstants() { }

    public static final String REQUEST_NAME_TEXT = "Welcome to the chat! Please, enter a name to start chatting.";
    public static final String NAME_DECLINED_TEXT = "Incorrect name, try another one.";
    public static final String NAME_ALREADY_IN_USE_TEXT = "This name is already in use, try another one.";
    public static final String USER_ALONE_IN_CHAT_TEXT = "You are alone in the chat.";
    public static final String NO_PARAMETERS_FOR_COMMAND = "You did not specify parameter(s) for the command.";

    public static final String LEFT_CHAT_TEMPLATE = "User %s has left the chat.";
    public static final String READY_TO_CHATTING_TEMPLATE = "User %s is ready to chatting.";
    public static final String USER_IS_RENAMED_TEMPLATE = "User %s is renamed to %s.";
    public static final String LIST_OF_USERS_TEMPLATE = "There is %d currently connected users:\n%s";

    public static final String NOT_VALID_COMMAND_TEMPLATE =
            "%s is not a valid command. In the chat, all messages that start with " +
                    "the / character are interpreted as commands just like in Slack.";

    public static final String SUCCESSFULLY_LOGGED_TEMPLATE =
            "Congratulations! You have successfully logged as %s. " +
                    "Type /help to show the list of commands.";
}
