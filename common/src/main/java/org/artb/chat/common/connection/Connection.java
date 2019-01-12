package org.artb.chat.common.connection;

import java.io.IOException;
import java.util.List;

public interface Connection {

    void send(String msg) throws IOException;

    void send(List<String> messages) throws IOException;

    String take() throws IOException;

    void notification();

    boolean connect() throws IOException;

    void close() throws IOException;
}
