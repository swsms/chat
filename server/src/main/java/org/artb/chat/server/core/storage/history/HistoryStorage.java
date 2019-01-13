package org.artb.chat.server.core.storage.history;

import org.artb.chat.common.message.Message;

import java.util.*;


public interface HistoryStorage {

    void add(Message msg);

    List<Message> history();
}
