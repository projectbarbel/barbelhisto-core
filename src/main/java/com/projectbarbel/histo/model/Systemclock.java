package com.projectbarbel.histo.model;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Systemclock {

    private Clock clock = Clock.systemDefaultZone();

    public Systemclock() {
    }
    public ZonedDateTime now() {
        return ZonedDateTime.now(clock);
    }

    public Systemclock useFixedClockAt(LocalDateTime date) {
        clock = Clock.fixed(date.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        return this;
    }

    public Systemclock useSystemDefaultZoneClock() {
        clock = Clock.systemDefaultZone();
        return this;
    }

}


