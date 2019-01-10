package org.artb.chat.common.settings;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Settings {

    @Parameter(names = "-host")
    private String host = "localhost";

    @Parameter(names = "-port")
    private int port = 8999;

    public static Settings fromArgsArray(String[] args) throws SettingsParseException {
        try {
            Settings settings = new Settings();
            JCommander.newBuilder()
                    .addObject(settings)
                    .build()
                    .parse(args);
            return settings;
        } catch (Exception e) {
            throw new SettingsParseException(e);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
