package org.projectbarbel.histo.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

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
                Arrays.asList(LocalDate.of(2010, 12, 1), LocalDate.of(2017, 12, 1), LocalDate.of(2020, 1, 1)));
        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 8, 0, 0));
    }

    @Test
    public void testApply() throws Exception {
        ResultSet<DefaultDocument> document = journal.retrieve(BarbelQueries.effectiveAt("docid1", BarbelHistoContext.getBarbelClock().now().toLocalDate()));
        assertTrue(document.iterator().hasNext());
        assertEquals(document.iterator().next().getBitemporalStamp().getEffectiveTime().from(), LocalDate.of(2017, 12, 1));
    }

    @Test
    public void testApply_laterDoc() throws Exception {
        ResultSet<DefaultDocument> document = journal.retrieve(BarbelQueries.effectiveAt("docid1", LocalDate.of(2021, 12, 1)));
        assertTrue(document.iterator().hasNext());
        assertEquals(document.iterator().next().getBitemporalStamp().getEffectiveTime().from(), LocalDate.of(2020, 1, 1));
    }

    @Test
    public void testApply_nonEffective() throws Exception {
        ResultSet<DefaultDocument> document = journal.retrieve(BarbelQueries.effectiveAt("docid1", LocalDate.of(2000, 12, 1)));
        assertFalse(document.iterator().hasNext());
    }
    
    @Test
    public void testApply_earlierDoc() throws Exception {
        ResultSet<DefaultDocument> document = journal.retrieve(BarbelQueries.effectiveAt("docid1", LocalDate.of(2012, 12, 1)));
        assertTrue(document.iterator().hasNext());
        assertEquals(document.iterator().next().getBitemporalStamp().getEffectiveTime().from(), LocalDate.of(2010, 12, 1));
    }

}
