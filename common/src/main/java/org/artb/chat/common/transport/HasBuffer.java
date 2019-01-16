package org.artb.chat.common.transport;

import java.io.IOException;

/**
 * Connections may have a temporary buffer
 */
public interface HasBuffer {

    void putInBuffer(String data);

    void flush() throws IOException;
}
