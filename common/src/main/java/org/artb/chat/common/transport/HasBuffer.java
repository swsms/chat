package org.artb.chat.common.transport;

import java.io.IOException;

public interface HasBuffer {

    void putInBuffer(String data);

    void flush() throws IOException;
}
