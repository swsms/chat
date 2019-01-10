package org.artb.chat.server.core.task;

import org.artb.chat.common.message.Message;

import java.util.UUID;

public class SendingTask {

    private final Message message;
    private final UUID clientId;
    private final Mode mode;

    private SendingTask(Message message, UUID clientId, Mode mode) {
        this.message = message;
        this.clientId = clientId;
        this.mode = mode;
    }

    public static SendingTask newBroadcastTask(Message message) {
        return new SendingTask(message, null, Mode.BROADCAST);
    }

    public static SendingTask newPersonalTask(Message message, UUID clientId) {
        return new SendingTask(message, clientId, Mode.PERSONAL);
    }

    public enum Mode {
        BROADCAST, PERSONAL
    }

    public Message getMessage() {
        return message;
    }

    public Mode getMode() {
        return mode;
    }

    public UUID getClientId() {
        return clientId;
    }

    @Override
    public String toString() {
        return "SendingTask{" +
                "message=" + message +
                ", clientId=" + clientId +
                ", mode=" + mode +
                '}';
    }
}
