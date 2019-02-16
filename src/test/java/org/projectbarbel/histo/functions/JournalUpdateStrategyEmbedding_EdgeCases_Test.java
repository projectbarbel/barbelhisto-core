package org.projectbarbel.histo.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.BarbelTestHelper;
import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.DocumentJournal.ProcessingState;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalObjectState;
import org.projectbarbel.histo.model.RecordPeriod;

public class JournalUpdateStrategyEmbedding_EdgeCases_Test {

	@SuppressWarnings("unused")
	private DocumentJournal journal;
	private BarbelHistoContext context;

	@BeforeEach
	public void setUp() {
		BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0));
		context = BarbelHistoBuilder.barbel().withMode(BarbelMode.POJO).withUser("testUser");
		journal = DocumentJournal
				.create(ProcessingState.INTERNAL, context,
						BarbelTestHelper.generateJournalOfDefaultPojos("someId", Arrays.asList(LocalDate.of(2016, 1, 1),
								LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))),
						"someId");
	}

	@AfterAll
	public static void tearDown() {
    	BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
	}
	
	@Test
	public void testApply_wrongId() throws Exception {
	}

	@SuppressWarnings("unused")
	private void assertTwoNewVersions(Bitemporal first, LocalDate from1, LocalDate until1, Bitemporal second,
			LocalDate from2, LocalDate until2, Bitemporal update) {

		assertEquals(first.getBitemporalStamp().getEffectiveTime().from(), from1);
		assertEquals(first.getBitemporalStamp().getEffectiveTime().until(), until1);
		assertEquals(first.getBitemporalStamp(), update.getBitemporalStamp());

		assertEquals(second.getBitemporalStamp().getEffectiveTime().from(), from2);
		assertEquals(second.getBitemporalStamp().getEffectiveTime().until(), until2);

		assertEquals(first.getBitemporalStamp().getRecordTime().getCreatedAt(),
				ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()));
		assertEquals(first.getBitemporalStamp().getRecordTime().getCreatedBy(), "testUser");
		assertEquals(first.getBitemporalStamp().getRecordTime().getInactivatedAt(), RecordPeriod.NOT_INACTIVATED);
		assertEquals(first.getBitemporalStamp().getRecordTime().getInactivatedBy(), RecordPeriod.NOBODY);
		assertEquals(first.getBitemporalStamp().getRecordTime().getState(), BitemporalObjectState.ACTIVE);

		assertEquals(second.getBitemporalStamp().getRecordTime().getCreatedAt(),
				ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()));
		assertEquals(second.getBitemporalStamp().getRecordTime().getCreatedBy(), "testUser");
		assertEquals(second.getBitemporalStamp().getRecordTime().getInactivatedAt(), RecordPeriod.NOT_INACTIVATED);
		assertEquals(second.getBitemporalStamp().getRecordTime().getInactivatedBy(), RecordPeriod.NOBODY);
		assertEquals(second.getBitemporalStamp().getRecordTime().getState(), BitemporalObjectState.ACTIVE);

	}
}
