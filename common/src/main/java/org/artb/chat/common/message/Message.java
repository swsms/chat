package org.artb.chat.common.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.artb.chat.common.json.ZdtFieldDeserializer;
import org.artb.chat.common.json.ZdtFieldSerializer;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Message {

    private String content;

    /**
     * This field stores where the message was actually processed on server.
     * It also contain zone to correct show the message on clients
     */
    @JsonSerialize(using = ZdtFieldSerializer.class)
    @JsonDeserialize(using = ZdtFieldDeserializer.class)
    private ZonedDateTime created;
    private MessageType type;
    private String sender;

    public Message() { }

    public Message(String content, MessageType type, String sender) {
        this.content = content;
        this.type = type;
        this.sender = sender;
        this.created = ZonedDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public ZonedDateTime getCreated() {
        return created;
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
