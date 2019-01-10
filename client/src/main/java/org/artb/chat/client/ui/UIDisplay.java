package org.artb.chat.client.ui;

import org.artb.chat.common.message.Message;

public interface UIDisplay {

    void print(Message msg);

    void print(String notification);
}
