package org.artb.chat.common.transport.tcpnio;

import org.artb.chat.common.Constants;
import org.artb.chat.common.Utils;
import org.artb.chat.common.transport.Connection;
import org.artb.chat.common.transport.HasBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public class TcpNioConnection implements Connection {

    private final UUID id;
    private final Selector selector;
    private final SocketChannel socket;

    private final ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
    private final Charset charset = StandardCharsets.UTF_8;
    private final Consumer<SwitchKeyInterestOpsTask> switchTaskConsumer;
    private final Queue<String> dataBuffer = new ConcurrentLinkedQueue<>();

    public TcpNioConnection(UUID id, Selector selector,
                            SocketChannel socket,
                            Consumer<SwitchKeyInterestOpsTask> switchTaskConsumer) {
        this.id = id;
        this.selector = selector;
        this.socket = socket;
        this.switchTaskConsumer = switchTaskConsumer;
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
        while (buf.hasRemaining()) {
            if (socket.write(buf) < 0) {
                throw new IOException("Cannot write data to channel");
            }
        }
        switchTaskConsumer.accept(new SwitchKeyInterestOpsTask(getSelectionKey(), OP_READ));
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
        if (key != null && key.isValid() && key.interestOps() == OP_READ) {
            switchTaskConsumer.accept(new SwitchKeyInterestOpsTask(key, OP_WRITE));
            selector.wakeup();
        }
    }

    @Override
    public void flush() throws IOException {
        String next;
        List<String> messages = new ArrayList<>();
        while ((next = dataBuffer.poll()) != null) {
            messages.add(next);
        }
        send(Utils.createBatch(messages));
    }

    @Override
    public void putInBuffer(String data) {
        dataBuffer.add(data);
    }

    @Override
    public synchronized void close() throws IOException {
        SelectionKey key = getSelectionKey();
        if (key != null) {
            key.cancel();
        }
        socket.close();
        dataBuffer.clear();
    }

    private SelectionKey getSelectionKey() {
        return socket.keyFor(selector);
    }

    @Override
    public UUID getId() {
        return id;
    }
}
