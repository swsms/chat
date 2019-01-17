package org.artb.chat.common.configs;

import com.beust.jcommander.Parameter;

public class BotConfig extends ServerConfig {

    @Parameter(names = "--bots")
    private int botsCount = 50;

    @Parameter(names = "--messages")
    private int msgCount = 50;

    @Parameter(names = "--min-wait-ms")
    private int minWaitMs = 500;

    @Parameter(names = "--max-wait-ms")
    private int maxWaitMs = 1000;

    public int getBotsCount() {
        return botsCount;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public int getMinWaitMs() {
        return minWaitMs;
    }

    public int getMaxWaitMs() {
        return maxWaitMs;
    }

    @Override
    public String toString() {
        return "BotConfig{" +
                "botsCount=" + botsCount +
                ", msgCount=" + msgCount +
                ", minWaitMs=" + minWaitMs +
                ", maxWaitMs=" + maxWaitMs +
                "} " + super.toString();
    }
}
