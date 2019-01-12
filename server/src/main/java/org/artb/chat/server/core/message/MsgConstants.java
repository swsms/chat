package org.artb.chat.server.core.message;

import org.artb.chat.common.message.Message;

import static org.artb.chat.common.message.Message.*;

public final class MsgConstants {

    public static final String REQUEST_NAME_MSG_TEXT = "Welcome to the chat! Please, enter a name to start chatting.";

    public static final String NAME_ACCEPTED_MSG_TEXT = "The name was accepted.";
    public static final String NAME_DECLINED_MSG_TEXT = "Incorrect name, try another one.";
    public static final String NAME_ALREADY_IN_USE_MSG_TEXT = "This name is already in use, try another one.";

    public static final String INCORRECT_FORMAT_MSG_TEXT =
            "The message has an incorrect format, " +
            "your client is possible broken.";

    public static final Message NAME_ACCEPTED_MSG = newServerMessage(NAME_ACCEPTED_MSG_TEXT);
    public static final Message REQUEST_NAME_MSG = newServerMessage(REQUEST_NAME_MSG_TEXT);
    public static final Message NAME_DECLINED_MSG = newServerMessage(NAME_DECLINED_MSG_TEXT);
    public static final Message NAME_ALREADY_IN_USE_MSG = newServerMessage(NAME_ALREADY_IN_USE_MSG_TEXT);

    private MsgConstants() { }
}
