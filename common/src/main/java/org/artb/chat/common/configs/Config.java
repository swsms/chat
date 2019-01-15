package org.artb.chat.common.configs;

import com.beust.jcommander.Parameter;

public class Config {

    @Parameter(names = "--host")
    private String host = "localhost";

    @Parameter(names = "--port")
    private int port = 8999;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Config{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
