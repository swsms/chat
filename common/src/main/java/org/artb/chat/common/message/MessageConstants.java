package org.artb.chat.common.message;

public final class MessageConstants {

    private MessageConstants() { }

    public static final String REQUEST_NAME_TEXT = "Welcome to the chat! Please, enter a name to start chatting.";
    public static final String USER_ALONE_IN_CHAT_TEXT = "You are alone in the chat.";
    public static final String CMD_NO_PARAMETERS = "You did not specify parameter(s) for the command.";
    public static final String SUCCESSFULLY_RENAMED = "You are successfully renamed.";


    public static final String LEFT_CHAT_TEMPLATE = "User %s has left the chat.";
    public static final String READY_TO_CHATTING_TEMPLATE = "User %s is ready to chatting.";
    public static final String USER_IS_RENAMED_TEMPLATE = "User %s is renamed to %s.";
    public static final String LIST_OF_USERS_TEMPLATE = "There are %d currently connected users:\n%s";

    public static final String NOT_VALID_COMMAND_TEMPLATE =
            "%s is not a valid command. In the chat, all messages that start with " +
                    "the / character are interpreted as commands just like in Slack.";

    public static final String SUCCESSFULLY_LOGGED_TEMPLATE =
            "Congratulations! You have successfully logged as %s. " +
                    "Type /help to show the list of commands.";
}
