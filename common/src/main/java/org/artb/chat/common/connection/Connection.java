package org.artb.chat.common.connection;

import java.io.IOException;

public interface Connection {

    void sendMessage(String msg) throws IOException;

    String takeMessage() throws IOException;

    void notification();

    boolean connect() throws IOException;

    void close() throws IOException;

    ConnectionConfig config();
}
