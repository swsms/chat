package org.artb.chat.client.ui;

public class DisplayConfig {

    private final boolean highlightServerMessage;
    private final boolean showDateTime;

    public DisplayConfig(boolean highlightServerMessage, boolean showDateTime) {
        this.highlightServerMessage = highlightServerMessage;
        this.showDateTime = showDateTime;
    }
}
