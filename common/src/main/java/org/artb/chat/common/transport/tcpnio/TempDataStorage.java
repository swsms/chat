package org.artb.chat.common.transport.tcpnio;

import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.artb.chat.common.Constants.STOP_CHAR;

public class TempDataStorage {

    private final Deque<String> deq = new ConcurrentLinkedDeque<>();

    public TempDataStorage() { }

    public Optional<String> extractNextData(String result) {
        int lastStopBytePosition = result.indexOf(STOP_CHAR);
        final String subresult;

        if (lastStopBytePosition <= 0) {
            deq.add(result);
            return Optional.empty();
        } else {
            StringBuilder accumulatedData = new StringBuilder();
            while (!deq.isEmpty()) {
                accumulatedData.append(deq.removeFirst());
            }
            accumulatedData.append(result, 0, lastStopBytePosition);
            subresult = accumulatedData.toString();
            if (lastStopBytePosition < result.length() - 1) {
                String tail = result.substring(lastStopBytePosition + 1); // exclude stop byte
                deq.add(tail);
            }
        }

        return Optional.of(subresult);
    }

    public int size() {
        return deq.size();
    }
}
