package org.artb.chat.common.message;


public class MessageFactory {

    /**
     * Such kinds of message are only used on the client-side.
     * The server knows the actual name and will reset it before broadcast.
     * This is used just to avoid nulls.
     */
    private final static String NOT_IMPORTANT_SENDER = "not-important";
    /**
     * Actually, all server messages have special type which determines that this is really a server.
     * This is used just to avoid nulls.
     */
    private final static String SERVER_SENDER = "server";


    public static Message newUserMessage(String content) {
        return new Message(content, MessageType.USER_TEXT, NOT_IMPORTANT_SENDER);
    }

    public static Message newUserMessage(String content, String sender) {
        return new Message(content, MessageType.USER_TEXT, sender);
    }

    public static Message newAuthMessage(String content) {
        return new Message(content, MessageType.TRY_AUTH, NOT_IMPORTANT_SENDER);
    }

    public static Message newServerMessage(String content) {
        return newServerMessage(content, MessageType.SERVER_TEXT);
    }

    public static Message newServerMessage(String content, MessageType type) {
        return new Message(content, type, SERVER_SENDER);
    }

    public static Message newServedUserMessage(String content, String sender) {
        return new Message(content, MessageType.USER_TEXT, sender);
    }
}
