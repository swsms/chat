package org.artb.chat.common.connection.tcpnio;

import org.artb.chat.common.Constants;
import org.artb.chat.common.connection.Connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public class TcpNioConnection implements Connection {

    private final Selector selector;
    private final SocketChannel socket;

    private final ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
    private final Charset charset = StandardCharsets.UTF_8;

    public TcpNioConnection(Selector selector, SocketChannel socket) {
        this.selector = selector;
        this.socket = socket;
    }

    @Override
    public boolean connect() throws IOException {
        if (socket.finishConnect()) {
            SelectionKey key = socket.keyFor(selector);
            key.interestOps(OP_READ);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void send(String data) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(data.getBytes(charset));
        if (socket.write(buf) < 0) {
            throw new IOException("Cannot write data to channel");
        }
        switchMode(OP_READ);
    }

    @Override
    public String take() throws IOException {
        buffer.clear();
        int read;
        StringBuilder builder = new StringBuilder();

        while ((read = socket.read(buffer)) > 0) {
            buffer.flip();
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes);
            buffer.clear();
            builder.append(new String(bytes, charset));
        }

        if (read < 0) {
            throw new IOException("Cannot read data from channel");
        }

        return builder.toString();
    }

    @Override
    public void notification() {
        switchMode(OP_WRITE);
    }

    @Override
    public synchronized void close() throws IOException {
        SelectionKey key = socket.keyFor(selector);
        if (key != null) {
            key.cancel();
        }
        socket.close();
    }

    private synchronized void switchMode(int targetMode) {
        SelectionKey key = socket.keyFor(selector);
        if (key.isValid()) {
            key.interestOps(targetMode);
            selector.wakeup();
        }
    }
}
