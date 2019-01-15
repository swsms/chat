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
import java.util.Queue;
import java.util.UUID;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public class TcpNioConnection implements Connection {

    private final UUID id;
    private final Selector selector;
    private final SocketChannel socket;

    private final ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
    private final Charset charset = StandardCharsets.UTF_8;
    private final Queue<SwitchKeyInterestOpsTask> switchTasks;

    public TcpNioConnection(UUID id, Selector selector,
                            SocketChannel socket,
                            Queue<SwitchKeyInterestOpsTask> switchTasks) {
        this.id = id;
        this.selector = selector;
        this.socket = socket;
        this.switchTasks = switchTasks;
    }

    @Override
    public boolean connect() throws IOException {
        if (socket.finishConnect()) {
            SelectionKey key = getSelectionKey();
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
        switchTasks.add(new SwitchKeyInterestOpsTask(getSelectionKey(), OP_READ));
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
        SelectionKey key = getSelectionKey();
        if (key.isValid() && key.interestOps() == OP_READ) {
            switchTasks.add(new SwitchKeyInterestOpsTask(key, OP_WRITE));
            selector.wakeup();
        }
    }

    @Override
    public void close() throws IOException {
        SelectionKey key = getSelectionKey();
        key.cancel();
        socket.close();
    }

    private SelectionKey getSelectionKey() {
        return socket.keyFor(selector);
    }

    @Override
    public UUID getId() {
        return id;
    }
}
