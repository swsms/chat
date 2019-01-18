package org.artb.chat.common.transport.tcpnio;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Just for tests
 */
class SimpleSelectionKey extends SelectionKey {
    private int ops;

    @Override
    public int interestOps() {
        return ops;
    }

    @Override
    public SelectionKey interestOps(int ops) {
        this.ops = ops;
        return this;
    }

    @Override
    public boolean isValid() {
        return true;
    }

        /*
           Now just ignore other methods
         */

    @Override
    public SelectableChannel channel() {
        return null;
    }

    @Override
    public Selector selector() {
        return null;
    }

    @Override
    public void cancel() {

    }

    @Override
    public int readyOps() {
        return 0;
    }
}
