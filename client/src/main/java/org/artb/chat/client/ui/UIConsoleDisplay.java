package org.artb.chat.client.ui;

import org.artb.chat.common.Utils;
import org.artb.chat.common.message.Message;
import org.artb.chat.common.message.MessageType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class UIConsoleDisplay implements UIDisplay {

    @Override
    public void print(Message msg) {
        if (msg.getType() != MessageType.USER_TEXT) {
            print(msg.getContent());
        } else {
            LocalDateTime when = Utils.toLocalTimeZoneWithoutNano(msg.getCreated());
            System.out.printf("[%s] %s: %s\n",
                    when.toString().replace('T', ' '),
                    msg.getSender(),
                    msg.getContent());
        }
    }

    @Override
    public void print(String notification) {
        System.out.println(notification);
    }
}
