package com.projectbarbel.histo.model;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Systemclock {

    private Clock clock = Clock.systemDefaultZone();

    public Systemclock() {
    }
    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    public void useFixedClockAt(LocalDateTime date) {
        clock = Clock.fixed(date.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    }

    public void useSystemDefaultZoneClock() {
        clock = Clock.systemDefaultZone();
    }

}


