package com.projectbarbel.histo.model;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Systemclock {

    private Clock clock;
    private ZoneId zone;

    public Systemclock(ZoneId zone) {
        this.clock = Clock.system(zone);
        this.zone = zone;
    }
    public LocalDateTime now() {
        return LocalDateTime.now(getClock());
    }

    public void useFixedClockAt(LocalDateTime date) {
        clock = Clock.fixed(date.atZone(zone).toInstant(), zone);
    }

    public void useSystemDefaultZoneClock() {
        clock = Clock.system(ZoneId.of(zone.getId()));
    }

    private Clock getClock() {
        return clock;
    }
}


