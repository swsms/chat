package org.artb.chat.common.connection.tcpnio;

import java.nio.channels.SelectionKey;
import java.util.Queue;

public class NioUtils {

    public static int switchKeyInterestOps(Queue<SwitchKeyInterestOpsTask> switchTasks) {
        SwitchKeyInterestOpsTask task;
        int switchedCount = 0;
        while ((task = switchTasks.poll()) != null) {
            SelectionKey key = task.getKey();
            if (key != null && key.isValid()) {
                key.interestOps(task.getOps());
                switchedCount++;
            }
        }
        return switchedCount;
    }
}
