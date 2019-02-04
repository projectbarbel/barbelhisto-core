package com.projectbarbel.histo.joutnal.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.BiFunction;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.resultset.ResultSet;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.joutnal.functions.BitemporalCollectionPreparedStatements;
import com.projectbarbel.histo.model.DefaultDocument;

public class BitemporalCollectionPreparedStatements_getActiveVersionEffectiveOn_ByDateTest {

    private DocumentJournal<DefaultDocument> journal;
    private BiFunction<IndexedCollection<DefaultDocument>, LocalDate, ResultSet<DefaultDocument>> function;

    @Before
    public void setUp() {
        journal = DocumentJournal.create(BarbelTestHelper.generateJournalOfDefaultValueObjects("docid1",
                Arrays.asList(LocalDate.of(2010, 12, 1), LocalDate.of(2017, 12, 1), LocalDate.of(2020, 1, 1))), "docid1");
        BarbelHistoContext.getClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 8, 0, 0));
        function = BitemporalCollectionPreparedStatements::getActiveVersionEffectiveOn_ByDate;
    }

    @Test
    public void testApply() throws Exception {
        ResultSet<DefaultDocument> document = function.apply(journal.collection(), BarbelHistoContext.getClock().now().toLocalDate());
        assertTrue(document.iterator().hasNext());
        assertEquals(document.iterator().next().getEffectiveFrom(), LocalDate.of(2017, 12, 1));
    }

    @Test
    public void testApply_laterDoc() throws Exception {
        ResultSet<DefaultDocument> document = function.apply(journal.collection(), LocalDate.of(2021, 12, 1));
        assertTrue(document.iterator().hasNext());
        assertEquals(document.iterator().next().getEffectiveFrom(), LocalDate.of(2020, 1, 1));
    }

    @Test
    public void testApply_nonEffective() throws Exception {
        ResultSet<DefaultDocument> document = function.apply(journal.collection(), LocalDate.of(2000, 12, 1));
        assertFalse(document.iterator().hasNext());
    }
    
    @Test
    public void testApply_earlierDoc() throws Exception {
        ResultSet<DefaultDocument> document = function.apply(journal.collection(), LocalDate.of(2012, 12, 1));
        assertTrue(document.iterator().hasNext());
        assertEquals(document.iterator().next().getEffectiveFrom(), LocalDate.of(2010, 12, 1));
    }

}
