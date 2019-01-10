package org.artb.chat.client.ui;

import org.artb.chat.common.message.Message;

import java.time.LocalDateTime;

public class UIConsoleDisplay implements UIDisplay {

    @Override
    public void print(Message msg) {
        switch (msg.getType()) {
            case SERVER_TEXT:
                print(msg.getContent());
                break;
            case USER_TEXT:
                LocalDateTime now = LocalDateTime.now().withNano(0);
                System.out.printf("[%s] %s: %s\n",
                        now.toString().replace('T', ' '),
                        msg.getSender(),
                        msg.getContent());
                break;
        }
    }

    @Override
    public void print(String notification) {
        System.out.println(notification);
    }
}
