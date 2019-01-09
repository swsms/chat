package org.artb.chat.common.message;

import java.util.UUID;

public class Message {

    private String content;
    private Type type;

    private String sender;
    private UUID client;

    public Message() { }

    private Message(String content, Type type, String sender, UUID clientId) {
        this.content = content;
        this.type = type;
        this.sender = sender;
        this.client = clientId;
    }

    public static Message newServerMessage(String content, UUID clientId) {
        return new Message(content, Type.SERVER_TEXT, "server", clientId);
    }

    public static Message newUserMessage(String content, String sender) {
        return new Message(content, Type.USER_TEXT, sender, null);
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

    public UUID getClient() {
        return client;
    }

    public void setClient(UUID client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "Message{" +
                "content='" + content + '\'' +
                ", type=" + type +
                ", sender='" + sender + '\'' +
                ", client=" + client +
                '}';
    }
}
