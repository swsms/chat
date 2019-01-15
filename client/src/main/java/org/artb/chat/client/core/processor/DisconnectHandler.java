package org.artb.chat.client.core.processor;

@FunctionalInterface
public interface DisconnectHandler {

    void handle();

    static void nothing() { }
}
