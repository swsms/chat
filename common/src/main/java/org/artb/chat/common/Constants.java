package org.artb.chat.common;

import java.time.format.DateTimeFormatter;

public final class Constants {

    public static final int BUFFER_SIZE = 4096;
    public static final char STOP_CHAR = (char) 0x00;
    public static final int HISTORY_SIZE = 100;
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public Constants() { }

}