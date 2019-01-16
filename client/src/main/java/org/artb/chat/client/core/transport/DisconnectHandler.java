package org.artb.chat.client.core.transport;

@FunctionalInterface
public interface DisconnectHandler {

    void handle();

    static void nothing() { }
}
