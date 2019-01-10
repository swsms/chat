package org.artb.chat.common.connection;

import org.artb.chat.common.Constants;

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
    private final SelectionKey key;

    private final ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
    private final Charset charset = StandardCharsets.UTF_8;

    public TcpNioConnection(Selector selector, SocketChannel socket) {
        this.selector = selector;
        this.socket = socket;
        this.key = socket.keyFor(selector);
    }

    @Override
    public boolean connect() throws IOException {
        if (socket.finishConnect()) {
            key.interestOps(OP_READ);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void sendMessage(String msg) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(msg.getBytes(charset));
        socket.write(buf);
        switchMode(OP_READ);
    }

    @Override
    public String takeMessage() throws IOException {
        // TODO check it
        socket.read(buffer);
        if (socket.read(buffer) < 0) {
            throw new IOException("Cannot read data from channel");
        }

        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        buffer.clear();

        return new String(bytes, charset);
    }

    @Override
    public void notification() {
        switchMode(OP_WRITE);
    }

    @Override
    public void close() throws IOException {
        socket.close();
        key.cancel();
    }

    private void switchMode(int targetMode) {
        SelectionKey key = socket.keyFor(selector);
        key.interestOps(targetMode);
        selector.wakeup();
    }
}
