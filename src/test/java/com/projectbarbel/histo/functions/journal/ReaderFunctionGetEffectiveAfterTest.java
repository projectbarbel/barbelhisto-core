package com.projectbarbel.histo.functions.journal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.DefaultDocument;

public class ReaderFunctionGetEffectiveAfterTest {

    private DocumentJournal<DefaultDocument> journal;
    private ReaderFunctionGetEffectiveAfter<DefaultDocument> function;

    @Before
    public void setUp() {
        journal = DocumentJournal.create(
                BarbelTestHelper.generateJournalOfDefaultValueObjects("docid1",
                        Arrays.asList(LocalDate.of(2010, 12, 1), LocalDate.of(2017, 12, 1), LocalDate.of(2020, 1, 1))),
                "docid1");
        BarbelHistoContext.getClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 8, 0, 0));
        function = new ReaderFunctionGetEffectiveAfter<DefaultDocument>();
    }

    @Test
    public void testApply_threeRecord_onePeriodAfterCurrent() throws Exception {
        IndexedCollection<DefaultDocument> documents = function.apply(journal.collection(),
                BarbelHistoContext.getClock().now().toLocalDate());
        assertTrue(documents.size() == 1);
        assertEquals(BitemporalCollectionPreparedStatements.getByID_orderByEffectiveFrom(documents, "docid1").iterator()
                .next().getEffectiveFrom(), LocalDate.of(2020, 1, 1));
    }

    @Test
    public void testApply_threeRecord_allAfter_DueDateOnBeginning() throws Exception {
        IndexedCollection<DefaultDocument> documents = function.apply(journal.collection(), LocalDate.of(2010, 12, 1));
        assertTrue(documents.size() == 3);
        assertEquals(LocalDate.of(2010, 12, 1), BitemporalCollectionPreparedStatements.getByID_orderByEffectiveFrom(documents, "docid1").iterator()
                .next().getEffectiveFrom());
    }

    @Test
    public void testApply_threeRecord_allAfter_DueDateBefore() throws Exception {
        IndexedCollection<DefaultDocument> documents = function.apply(journal.collection(), LocalDate.of(2010, 11, 1));
        assertTrue(documents.size() == 3);
        assertEquals(LocalDate.of(2010, 12, 1), BitemporalCollectionPreparedStatements.getByID_orderByEffectiveFrom(documents, "docid1").iterator()
                .next().getEffectiveFrom());
    }

    @Test
    public void testApply_threeRecord_twoAfter() throws Exception {
        IndexedCollection<DefaultDocument> documents = function.apply(journal.collection(), LocalDate.of(2011, 12, 1));
        assertTrue(documents.size() == 2);
        assertEquals(LocalDate.of(2017, 12, 1), BitemporalCollectionPreparedStatements.getByID_orderByEffectiveFrom(documents, "docid1").iterator()
                .next().getEffectiveFrom());
    }

    @Test
    public void testApply_threeRecord_oneAfter() throws Exception {
        IndexedCollection<DefaultDocument> documents = function.apply(journal.collection(), LocalDate.of(2021, 12, 1));
        assertTrue(documents.size() == 0);
    }

}
