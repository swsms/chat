package org.artb.chat.server.core.message.consumer;

import java.util.List;

/**
 * Represent an abstraction that consumes data.
 * It may be a connection or an intermediate storage.
 */
public interface DataConsumer {

    void consume(String data);

    void consume(List<String> data);
}
