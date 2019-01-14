package org.artb.chat.server.core.consumer;


import java.util.List;
import java.util.UUID;

/**
 * Represent an abstraction that consumes data.
 * It may be a connection or an intermediate storage like DB or file.
 */
public interface DataConsumer<T> {

    void consume(UUID userId, T data);

    void consume(UUID userId, List<T> dataList);
}
