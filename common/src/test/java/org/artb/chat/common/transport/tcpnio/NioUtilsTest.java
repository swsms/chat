package org.artb.chat.common.transport.tcpnio;

import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.*;


public class NioUtilsTest {

    @Test
    public void testSwitchKeyInterestOps() {
        final int nTasks = 10;

        Queue<SwitchKeyInterestOpsTask> switchTasks = Stream
                .generate(SimpleSelectionKey::new)
                .limit(nTasks)
                .map(key -> new SwitchKeyInterestOpsTask(key, 1))
                .collect(Collectors.toCollection(LinkedList::new));

        switchTasks.forEach((task) -> assertEquals(task.getKey().interestOps(), 0));

        int switched = NioUtils.switchKeyInterestOps(switchTasks);

        switchTasks.forEach((task) -> assertEquals(task.getKey().interestOps(), 1));
        assertEquals(switched, nTasks);
    }
}
