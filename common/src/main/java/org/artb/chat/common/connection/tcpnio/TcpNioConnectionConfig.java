package org.artb.chat.common.connection.tcpnio;

import org.artb.chat.common.connection.ConnectionConfig;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class TcpNioConnectionConfig implements ConnectionConfig {

    private final SocketChannel socket;
    private final SelectionKey key;

    public TcpNioConnectionConfig(SocketChannel socket, SelectionKey key) {
        this.socket = socket;
        this.key = key;
    }

    public SocketChannel getSocket() {
        return socket;
    }

    public SelectionKey getKey() {
        return key;
    }
}
