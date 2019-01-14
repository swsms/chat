package org.artb.chat.common.connection;

import java.io.IOException;

public interface HasBuffer {

    void putInBuffer(String data);

    void flush() throws IOException;
}
