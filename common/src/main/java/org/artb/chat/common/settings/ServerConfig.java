package org.artb.chat.common.settings;

import com.beust.jcommander.Parameter;

public class ServerConfig extends Config {

    @Parameter(names = "--msg-processors")
    private int msgProcessors = 2;

    @Parameter(names = "--con-managers")
    private int connectionManagers = 1;

    public int getMsgProcessors() {
        return msgProcessors;
    }

    public int getConnectionManagers() {
        return connectionManagers;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "host='" + getHost() +
                ", port=" + getPort() +
                ", msgProcessors=" + msgProcessors +
                ", connectionManagers=" + connectionManagers +
                '}';
    }
}
