package org.artb.chat.client.ui;

import org.artb.chat.common.command.CommandInfo;

import java.time.LocalDateTime;
import java.util.List;

public interface UIDisplay {

    void printUserText(LocalDateTime when, String sender, String text);

    void printUserList(List<String> users);

    void printHelp(List<CommandInfo> commands);

    void print(String line);

}
