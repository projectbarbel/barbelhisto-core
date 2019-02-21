package org.projectbarbel.histo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelHistoContext;

public class RecordPeriodTest {

	@Test
	public void testCreateActive() throws Exception {
		LocalDateTime now = LocalDateTime.now();
		BarbelHistoContext.getBarbelClock().useFixedClockAt(now);
		RecordPeriod period = RecordPeriod.createActive(BarbelHistoBuilder.barbel());
		assertEquals(period.getCreatedAt(), ZonedDateTime.of(now, ZoneId.systemDefault()));
		assertEquals(period.getCreatedBy(), BarbelHistoBuilder.SYSTEM);
		assertEquals(period.getInactivatedAt(), RecordPeriod.NOT_INACTIVATED);
		assertEquals(period.getInactivatedBy(), RecordPeriod.NOBODY);
		assertEquals(period.getState(), BitemporalObjectState.ACTIVE);
	}

	@Test
	public void testCompileState() throws Exception {
		assertEquals(BitemporalObjectState.ACTIVE,
				RecordPeriod.createActive(BarbelHistoBuilder.barbel()).compileState());
	}

	@Test
	public void testCompileStateInactive() throws Exception {
		assertEquals(BitemporalObjectState.INACTIVE, RecordPeriod.createActive(BarbelHistoBuilder.barbel())
				.inactivate(BarbelHistoBuilder.barbel()).compileState());
	}

	@Test
	public void testEquals() throws Exception {
		LocalDateTime now = LocalDateTime.now();
		BarbelHistoContext.getBarbelClock().useFixedClockAt(now);
		RecordPeriod period1 = RecordPeriod.createActive(BarbelHistoBuilder.barbel());
		RecordPeriod period2 = RecordPeriod.createActive(BarbelHistoBuilder.barbel());
		assertEquals(period1, period2);
	}

	@Test
	public void testToString() throws Exception {
		assertEquals("RecordPeriod",
				RecordPeriod.createActive(BarbelHistoBuilder.barbel()).toString().substring(0, 12));
	}
}
