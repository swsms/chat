package org.artb.chat.common.transport;

import org.artb.chat.common.Identifiable;

import java.io.IOException;

public interface Connection extends Identifiable, HasBuffer  {

    void send(String data) throws IOException;

    String take() throws IOException;

    boolean connect() throws IOException;

    void close() throws IOException;
}
