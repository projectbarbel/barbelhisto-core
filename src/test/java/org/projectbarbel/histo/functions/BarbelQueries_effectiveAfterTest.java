package org.projectbarbel.histo.functions;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;
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

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.resultset.ResultSet;

public class BarbelQueries_effectiveAfterTest {

    private IndexedCollection<DefaultDocument> journal;

    @BeforeEach
    public void setUp() {
        journal = BarbelTestHelper.generateJournalOfDefaultDocuments("docid1",
                Arrays.asList(
                        ZonedDateTime.parse("2010-12-01T00:00:00Z"), ZonedDateTime.parse("2017-12-01T00:00:00Z"), ZonedDateTime.parse("2020-01-01T00:00:00Z")));
        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 8, 0, 0).atZone(ZoneId.of("Z")));
    }
    
    @AfterAll
    public static void tearDown() {
        BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
    }

    @Test
    public void testApply_threeRecord_onePeriodAfterCurrent() throws Exception {
        ResultSet<DefaultDocument> documents = journal
                .retrieve(BarbelQueries.effectiveAfter("docid1", BarbelHistoContext.getBarbelClock().now()));
        assertTrue(documents.size() == 1);
        assertEquals(documents.iterator().next().getBitemporalStamp().getEffectiveTime().from(),
                ZonedDateTime.parse("2020-01-01T00:00:00Z"));
    }

    @Test
    public void testApply_threeRecord_allAfter_DueDateOnBeginning() throws Exception {
        ResultSet<DefaultDocument> documents = journal.retrieve(
                BarbelQueries.effectiveAfter("docid1", ZonedDateTime.parse("2010-12-01T00:00:00Z")),
                queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))));
        assertTrue(documents.size() == 3);
        assertEquals(ZonedDateTime.parse("2010-12-01T00:00:00Z"),
                documents.iterator().next().getBitemporalStamp().getEffectiveTime().from());
    }

    @Test
    public void testApply_threeRecord_allAfter_DueDateBefore() throws Exception {
        ResultSet<DefaultDocument> documents = journal.retrieve(
                BarbelQueries.effectiveAfter("docid1", ZonedDateTime.parse("2010-11-01T00:00:00Z")),
                queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))));
        assertTrue(documents.size() == 3);
        assertEquals(ZonedDateTime.parse("2010-12-01T00:00:00Z"),
                documents.iterator().next().getBitemporalStamp().getEffectiveTime().from());
    }

    @Test
    public void testApply_threeRecord_twoAfter() throws Exception {
        ResultSet<DefaultDocument> documents = journal.retrieve(
                BarbelQueries.effectiveAfter("docid1", ZonedDateTime.parse("2011-12-01T00:00:00Z")),
                queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))));
        assertTrue(documents.size() == 2);
        assertEquals(ZonedDateTime.parse("2017-12-01T00:00:00Z"),
                documents.iterator().next().getBitemporalStamp().getEffectiveTime().from());
    }

    @Test
    public void testApply_threeRecord_oneAfter() throws Exception {
        ResultSet<DefaultDocument> documents = journal.retrieve(
                BarbelQueries.effectiveAfter("docid1", ZonedDateTime.parse("2021-12-01T00:00:00Z")),
                queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))));
        assertTrue(documents.size() == 0);
    }

}
