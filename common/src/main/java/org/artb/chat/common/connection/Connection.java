package org.artb.chat.common.connection;

import org.artb.chat.common.Identifiable;

import java.io.IOException;

public interface Connection extends Identifiable  {

    void send(String data) throws IOException;

    String take() throws IOException;

    boolean connect() throws IOException;

    void close() throws IOException;

    void notification();
}
