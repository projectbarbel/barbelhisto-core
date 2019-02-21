package org.projectbarbel.histo.model;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoContext;

/**
 * {@link BarbelHisto}s clock implementation. Can also be used for testing. Call
 * {@link BarbelHistoContext#getBarbelClock()} to fix time when performing
 * specific tests.
 * 
 * @author Niklas Schlimm
 *
 */
public class Systemclock {

    private Clock clock = Clock.systemDefaultZone();

    public ZonedDateTime now() {
        return ZonedDateTime.now(clock);
    }

    public LocalDate today() {
        return ZonedDateTime.now(clock).toLocalDate();
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
