package org.artb.chat.common.message;

public class Message {

    private String content;
    private Type type;
    private String sender;

    public Message() { }

    private Message(String content, Type type, String sender) {
        this.content = content;
        this.type = type;
        this.sender = sender;
    }

    public static Message newServerMessage(String content) {
        return new Message(content, Type.SERVER_TEXT, "server");
    }

    public static Message newUserMessage(String content, String sender) {
        return new Message(content, Type.USER_TEXT, sender);
    }

    public enum Type {
        SERVER_TEXT, USER_TEXT
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "Message{" +
                "content='" + content + '\'' +
                ", type=" + type +
                ", sender='" + sender + '\'' +
                '}';
    }
}
