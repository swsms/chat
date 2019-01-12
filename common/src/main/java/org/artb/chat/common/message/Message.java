package org.artb.chat.common.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.artb.chat.common.json.ZdtFieldDeserializer;
import org.artb.chat.common.json.ZdtFieldSerializer;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Message {

    private String content;
    private Type type;
    private String sender;

    /**
     * This field stores where the message was actually processed on server.
     * It also contain zone to correct show the message on clients
     */
    @JsonSerialize(using = ZdtFieldSerializer.class)
    @JsonDeserialize(using = ZdtFieldDeserializer.class)
    private ZonedDateTime served = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

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

    /**
     * It is used on a client side, the server knows actual client's name
     */
    public static Message newUserMessage(String content) {
        return new Message(content, Type.USER_TEXT, "client");
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

    public ZonedDateTime getServed() {
        return served;
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
