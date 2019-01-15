package com.projectbarbel.histo.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;

/**
 * Understand the following facts:
 * - there is only one point in time, regardless of the time zone
 * - the same point in time (Instant) leads to different date/time representations visible to the user
 * - the same date/time representation leads to different point in times (Instants)
 * 
 * Here's an example:
 *                                        Time Zone: Europe/Berlin
 *                                       Local Date: 2019-01-13T18:46:00.000	
 *          Instant of Local Date in Epoche Seconds: 1547401560					<- point in time
 *                UTC Date/Time of previous Instant: 2019-01-13T17:46:00.000
 *  UTC Instant representing the initial Local Date: 1547405160					-> 2019-01-13T18:46:00.000 UTC
 *        Difference betweent Local und UTC Instant: -3600
 * 
 * 
 * @author niklasschlimm
 *
 */
public class JavaDateTimeAPITest {

	private ZoneId berlinTimeZone = ZoneId.of("Europe/Berlin");
	private ZoneId utcTimeZone = ZoneId.of("Z");

	/**
	 * Understanding three different types {@link java.time.Clock}
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testClock() throws InterruptedException {
		Clock wallClock = Clock.systemDefaultZone(); // a clock like yours on the wall, with your local time
		printOutDateTime(wallClock);
		Clock utcClock = Clock.systemUTC(); // reference clock
		printOutDateTime(utcClock);
		Clock internationalClock = Clock.system(ZoneId.of("America/New_York")); // a clock like those in an airport,
																				// that show the time in various
																				// countries
		printOutDateTime(internationalClock);
		ZoneId.getAvailableZoneIds().forEach(System.out::println); // all time zones
	}

	private void printOutDateTime(Clock clock) throws InterruptedException {
		System.out.format("%50s%1s", "Time Zone: ", clock.getZone().getId()+"\n");
		LocalDateTime localdatetime = LocalDateTime.now(clock);
		System.out.format("%50s%1s", "Local Date: ", localdatetime+"\n");
		Instant instant = localdatetime.atZone(clock.getZone()).toInstant();
		long localinstantEpocheSecond = instant.getEpochSecond();
		System.out.format("%50s%1s", "Instant of Local Date in Epoche Seconds: ", localinstantEpocheSecond+"\n");
		System.out.format("%50s%1s", "UTC Date/Time representation of previous Instant: ", LocalDateTime.ofInstant(instant, ZoneId.of("Z"))+"\n");
		long utcinstantEpocheSecond = localdatetime.atZone(utcTimeZone).toInstant().getEpochSecond();
		System.out.format("%50s%1s", "UTC Instant representing the initial Local Date: ", utcinstantEpocheSecond+"\n");
		System.out.format("%50s%1s", "Difference betweent Local und UTC Instant: ", (localinstantEpocheSecond-utcinstantEpocheSecond)+"\n\n");
	}

	/**
	 * Change Time Zone without changing the Local Date Time 
	 * -> you want your user to see the same date/time, regardless of the time zone.
	 */
	@Test
	public void testChangingTimeZoneOnly() {
		Clock internationalClock = Clock.system(berlinTimeZone);
		LocalDateTime localdatetime = LocalDateTime.now(internationalClock);
		ZonedDateTime berlinDateTime = localdatetime.atZone(berlinTimeZone);
		ZonedDateTime utcDateTime = berlinDateTime.withZoneSameInstant(utcTimeZone); // point in time remains, different local dates
		Instant berlinInstant = berlinDateTime.toInstant();
		Instant utcInstant = utcDateTime.toInstant();
		assertEquals(berlinInstant, utcInstant); 
		assertNotEquals(utcDateTime.toLocalDateTime(), berlinDateTime.toLocalDateTime());
	}

	/**
	 * Representing a local date as an UTC instant
	 * -> you want to store the "UTC time stamp equivalent" to the date visible for the user.
	 */
	@Test
	public void testLocalDateToUTCInstant() {
		Clock internationalClock = Clock.system(berlinTimeZone);
		LocalDate localdate = LocalDate.now(internationalClock);
		ZonedDateTime berlinDateTime = localdate.atStartOfDay(berlinTimeZone);
		ZonedDateTime utcDateTime = berlinDateTime.withZoneSameLocal(utcTimeZone); // changes point in time, same local dates
		Instant berlinInstant = berlinDateTime.toInstant();
		Instant utcInstant = utcDateTime.toInstant();
		assertNotEquals(berlinInstant, utcInstant);
		assertEquals(utcDateTime.toLocalDateTime(), berlinDateTime.toLocalDateTime());
	}

	/**
	 * Representing a local date time as an UTC instant
	 * -> you want to store the "UTC time stamp equivalent" to the date/time visible for the user.
	 */
	@Test
	public void testLocalDateTimeToUTCInstant() {
		Clock internationalClock = Clock.system(berlinTimeZone);
		LocalDateTime localdatetime = LocalDateTime.now(internationalClock);
		ZonedDateTime berlinDateTime = localdatetime.atZone(berlinTimeZone);
		ZonedDateTime utcDateTime = berlinDateTime.withZoneSameLocal(utcTimeZone); // changes point in time, same local dates
		Instant berlinInstant = berlinDateTime.toInstant();
		Instant utcInstant = utcDateTime.toInstant();
		assertNotEquals(berlinInstant, utcInstant);
		assertEquals(utcDateTime.toLocalDateTime(), berlinDateTime.toLocalDateTime());
	}

	/**
	 * Representing an UTC instant as local date/time
	 * -> you want to present the local/date time equivalent of an UTC instant to the user
	 */
	@Test
	public void testUTCInstantToLocalDateTime() {
		Clock utcClock = Clock.systemUTC();
		Instant utcInstant = Instant.now(utcClock); // System UTC time stamp
		ZonedDateTime utcDateTime = ZonedDateTime.ofInstant(utcInstant, utcTimeZone);
		ZonedDateTime berlinDateTime = utcDateTime.withZoneSameLocal(berlinTimeZone); // changes point in time, same local dates
		assertEquals(utcDateTime.toLocalDateTime(), berlinDateTime.toLocalDateTime());
		assertNotEquals(utcDateTime.toInstant(), berlinDateTime.toInstant());
	}

}

