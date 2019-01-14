package org.artb.chat.common.connection.tcpnio;

import java.nio.channels.SelectionKey;

public class SwitchKeyInterestOpsTask {

    private final SelectionKey key;
    private final int ops;

    public SwitchKeyInterestOpsTask(SelectionKey key, int ops) {
        this.key = key;
        this.ops = ops;
    }

    public SelectionKey getKey() {
        return key;
    }

    public int getOps() {
        return ops;
    }

    @Override
    public String toString() {
        return "SwitchKeyInterestOps{" +
                "key=" + key +
                ", ops=" + ops +
                '}';
    }
}
