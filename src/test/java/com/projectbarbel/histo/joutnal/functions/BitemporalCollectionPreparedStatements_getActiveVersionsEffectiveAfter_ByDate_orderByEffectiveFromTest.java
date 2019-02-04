package com.projectbarbel.histo.joutnal.functions;

import static org.junit.Assert.assertEquals;
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

public class BitemporalCollectionPreparedStatements_getActiveVersionsEffectiveAfter_ByDate_orderByEffectiveFromTest {

    private DocumentJournal<DefaultDocument> journal;
    private BiFunction<IndexedCollection<DefaultDocument>, LocalDate, ResultSet<DefaultDocument>> function;

    @Before
    public void setUp() {
        journal = DocumentJournal.create(
                BarbelTestHelper.generateJournalOfDefaultValueObjects("docid1",
                        Arrays.asList(LocalDate.of(2010, 12, 1), LocalDate.of(2017, 12, 1), LocalDate.of(2020, 1, 1))),
                "docid1");
        BarbelHistoContext.getClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 8, 0, 0));
        function = BitemporalCollectionPreparedStatements::getActiveVersionsEffectiveAfter_ByDate_orderByEffectiveFrom;
    }

    @Test
    public void testApply_threeRecord_onePeriodAfterCurrent() throws Exception {
        ResultSet<DefaultDocument> documents = function.apply(journal.collection(),
                BarbelHistoContext.getClock().now().toLocalDate());
        assertTrue(documents.size() == 1);
        assertEquals(documents.iterator().next().getEffectiveFrom(), LocalDate.of(2020, 1, 1));
    }

    @Test
    public void testApply_threeRecord_allAfter_DueDateOnBeginning() throws Exception {
        ResultSet<DefaultDocument> documents = function.apply(journal.collection(), LocalDate.of(2010, 12, 1));
        assertTrue(documents.size() == 3);
        assertEquals(LocalDate.of(2010, 12, 1), documents.iterator().next().getEffectiveFrom());
    }

    @Test
    public void testApply_threeRecord_allAfter_DueDateBefore() throws Exception {
        ResultSet<DefaultDocument> documents = function.apply(journal.collection(), LocalDate.of(2010, 11, 1));
        assertTrue(documents.size() == 3);
        assertEquals(LocalDate.of(2010, 12, 1), documents.iterator().next().getEffectiveFrom());
    }

    @Test
    public void testApply_threeRecord_twoAfter() throws Exception {
        ResultSet<DefaultDocument> documents = function.apply(journal.collection(), LocalDate.of(2011, 12, 1));
        assertTrue(documents.size() == 2);
        assertEquals(LocalDate.of(2017, 12, 1), documents.iterator().next().getEffectiveFrom());
    }

    @Test
    public void testApply_threeRecord_oneAfter() throws Exception {
        ResultSet<DefaultDocument> documents = function.apply(journal.collection(), LocalDate.of(2021, 12, 1));
        assertTrue(documents.size() == 0);
    }

}
