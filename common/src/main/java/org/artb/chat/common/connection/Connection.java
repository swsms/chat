package org.artb.chat.common.connection;

import java.io.IOException;

public interface Connection {

    void send(String msg) throws IOException;

    String take() throws IOException;

    void notification();

    boolean connect() throws IOException;

    void close() throws IOException;
}
