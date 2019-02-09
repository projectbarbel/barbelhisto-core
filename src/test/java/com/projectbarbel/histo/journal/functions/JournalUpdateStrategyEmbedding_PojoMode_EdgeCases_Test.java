package com.projectbarbel.histo.journal.functions;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoBuilder;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelMode;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalObjectState;
import com.projectbarbel.histo.model.RecordPeriod;
import com.projectbarbel.histo.model.Systemclock;

public class JournalUpdateStrategyEmbedding_PojoMode_EdgeCases_Test {

    @SuppressWarnings("unused")
    private DocumentJournal journal;
    @SuppressWarnings("unused")
    private BarbelHistoContext context;

    @Before
    public void setUp() {
        journal = DocumentJournal
                .create(BarbelTestHelper.generateJournalOfDefaultPojos("someId", Arrays.asList(LocalDate.of(2016, 1, 1),
                        LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))), "someId");
        context = BarbelHistoBuilder.barbel().withMode(BarbelMode.POJO)
                .withClock(new Systemclock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0)))
                .withUser("testUser");
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
