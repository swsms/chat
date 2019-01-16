package org.artb.chat.client.ui;

import java.time.LocalDateTime;
import java.util.List;

public interface UIDisplay {

    void printUserText(LocalDateTime when, String sender, String text);

    void printUserList(List<String> users);

    void print(String line);

}
