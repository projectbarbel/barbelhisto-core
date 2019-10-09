package org.projectbarbel.histo.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

public class BarbelQueries_effectiveAtTest {

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
    public void testApply() throws Exception {
        ResultSet<DefaultDocument> document = journal.retrieve(BarbelQueries.effectiveAt("docid1", BarbelHistoContext.getBarbelClock().now()));
        assertTrue(document.iterator().hasNext());
        assertEquals(document.iterator().next().getBitemporalStamp().getEffectiveTime().from(), ZonedDateTime.parse("2017-12-01T00:00:00Z"));
    }

    @Test
    public void testApply_laterDoc() throws Exception {
        ResultSet<DefaultDocument> document = journal.retrieve(BarbelQueries.effectiveAt("docid1", ZonedDateTime.parse("2021-12-01T00:00:00Z")));
        assertTrue(document.iterator().hasNext());
        assertEquals(document.iterator().next().getBitemporalStamp().getEffectiveTime().from(), ZonedDateTime.parse("2020-01-01T00:00:00Z"));
    }

    @Test
    public void testApply_nonEffective() throws Exception {
        ResultSet<DefaultDocument> document = journal.retrieve(BarbelQueries.effectiveAt("docid1", ZonedDateTime.parse("2000-12-01T00:00:00Z")));
        assertFalse(document.iterator().hasNext());
    }
    
    @Test
    public void testApply_earlierDoc() throws Exception {
        ResultSet<DefaultDocument> document = journal.retrieve(BarbelQueries.effectiveAt("docid1", ZonedDateTime.parse("2012-12-01T00:00:00Z")));
        assertTrue(document.iterator().hasNext());
        assertEquals(document.iterator().next().getBitemporalStamp().getEffectiveTime().from(), ZonedDateTime.parse("2010-12-01T00:00:00Z"));
    }

}
