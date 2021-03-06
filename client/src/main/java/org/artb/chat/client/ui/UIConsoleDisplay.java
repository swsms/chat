package org.artb.chat.client.ui;

import org.artb.chat.common.command.CommandInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.artb.chat.common.message.MessageConstants.LIST_OF_USERS_TEMPLATE;
import static org.artb.chat.common.message.MessageConstants.USER_ALONE_IN_CHAT_TEXT;

public class UIConsoleDisplay implements UIDisplay {

    @Override
    public void printUserText(LocalDateTime when, String sender, String text) {
        print(String.format("[%s] %s: %s",
                when.toString().replace('T', ' '), sender, text));
    }

    @Override
    public void printUserList(List<String> users) {
        String joinedUsers = String.join("\n", users);
        String text = users.size() != 1 ?
                String.format(LIST_OF_USERS_TEMPLATE, users.size(), joinedUsers) :
                USER_ALONE_IN_CHAT_TEXT;
        print(text);
    }

    @Override
    public void printHelp(List<CommandInfo> commands) {
        print(commands.stream()
                .map(cmd -> cmd.getCommand() + "\t" + cmd.getDescription())
                .collect(Collectors.joining("\n")));
    }

    @Override
    public void print(String line) {
        System.out.println(line);
    }
}
