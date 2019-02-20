package org.projectbarbel.histo;

import java.time.format.DateTimeFormatter;

import org.projectbarbel.histo.model.Systemclock;

public class BarbelConstants {

    public static final String SYSTEM = "SYSTEM";
    public static final String SYSTEMACTIVITY = "SYSTEMACTIVITY";
    public static final Systemclock CLOCK = new Systemclock();
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

}
