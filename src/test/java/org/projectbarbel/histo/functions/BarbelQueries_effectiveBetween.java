package org.projectbarbel.histo.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelQueries;
import org.projectbarbel.histo.BarbelTestHelper;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.model.EffectivePeriod;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.resultset.ResultSet;

public class BarbelQueries_effectiveBetween {

    private IndexedCollection<DefaultDocument> journal;

    @BeforeEach
    public void setUp() {
        journal = BarbelTestHelper.generateJournalOfDefaultDocuments("docid1",
                Arrays.asList(ZonedDateTime.parse("2010-12-01T00:00:00Z"), ZonedDateTime.parse("2017-12-01T00:00:00Z"), ZonedDateTime.parse("2020-01-01T00:00:00Z")));
        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 8, 0, 0).atZone(ZoneId.of("Z")));
    }

    @AfterAll
    public static void tearDown() {
        BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
    }

    @Test
    public void testApply_threeRecord_onePeriodBetween() throws Exception {
        ResultSet<DefaultDocument> documents = journal.retrieve(BarbelQueries.effectiveBetween("docid1",
                EffectivePeriod.of(ZonedDateTime.parse("2010-12-02T00:00:00Z"), ZonedDateTime.parse("2020-01-02T00:00:00Z"))));
        assertTrue(documents.size() == 1);
        assertEquals(documents.iterator().next().getBitemporalStamp().getEffectiveTime().from(),
                ZonedDateTime.parse("2017-12-01T00:00:00Z"));
    }

    @Test
    public void testApply_threeRecord_allBetween() throws Exception {
        ResultSet<DefaultDocument> documents = journal.retrieve(BarbelQueries.effectiveBetween("docid1",
                EffectivePeriod.of(ZonedDateTime.parse("2010-11-01T00:00:00Z"), EffectivePeriod.INFINITE)));
        assertTrue(documents.size() == 3);
    }

}
