package org.artb.chat.server.core.transport.tcpnio;

import java.util.UUID;

/**
 * Represents a receiver that encapsulates socket connections or something else
 */
public interface DataReceiver {

    void accept(UUID clientId, String data);
}
