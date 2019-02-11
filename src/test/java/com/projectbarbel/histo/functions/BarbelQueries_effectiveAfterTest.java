package com.projectbarbel.histo.functions;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.resultset.ResultSet;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelQueries;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.model.DefaultDocument;

public class BarbelQueries_effectiveAfterTest {

    private IndexedCollection<DefaultDocument> journal;

    @BeforeEach
    public void setUp() {
        journal = BarbelTestHelper.generateJournalOfDefaultDocuments("docid1",
                Arrays.asList(LocalDate.of(2010, 12, 1), LocalDate.of(2017, 12, 1), LocalDate.of(2020, 1, 1)));
        BarbelHistoContext.getDefaultClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 8, 0, 0));
    }

    @Test
    public void testApply_threeRecord_onePeriodAfterCurrent() throws Exception {
        ResultSet<DefaultDocument> documents = journal
                .retrieve(BarbelQueries.effectiveAfter("docid1", BarbelHistoContext.getDefaultClock().now().toLocalDate()));
        assertTrue(documents.size() == 1);
        assertEquals(documents.iterator().next().getBitemporalStamp().getEffectiveTime().from(),
                LocalDate.of(2020, 1, 1));
    }

    @Test
    public void testApply_threeRecord_allAfter_DueDateOnBeginning() throws Exception {
        ResultSet<DefaultDocument> documents = journal.retrieve(
                BarbelQueries.effectiveAfter("docid1", LocalDate.of(2010, 12, 1)),
                queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))));
        assertTrue(documents.size() == 3);
        assertEquals(LocalDate.of(2010, 12, 1),
                documents.iterator().next().getBitemporalStamp().getEffectiveTime().from());
    }

    @Test
    public void testApply_threeRecord_allAfter_DueDateBefore() throws Exception {
        ResultSet<DefaultDocument> documents = journal.retrieve(
                BarbelQueries.effectiveAfter("docid1", LocalDate.of(2010, 11, 1)),
                queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))));
        assertTrue(documents.size() == 3);
        assertEquals(LocalDate.of(2010, 12, 1),
                documents.iterator().next().getBitemporalStamp().getEffectiveTime().from());
    }

    @Test
    public void testApply_threeRecord_twoAfter() throws Exception {
        ResultSet<DefaultDocument> documents = journal.retrieve(
                BarbelQueries.effectiveAfter("docid1", LocalDate.of(2011, 12, 1)),
                queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))));
        assertTrue(documents.size() == 2);
        assertEquals(LocalDate.of(2017, 12, 1),
                documents.iterator().next().getBitemporalStamp().getEffectiveTime().from());
    }

    @Test
    public void testApply_threeRecord_oneAfter() throws Exception {
        ResultSet<DefaultDocument> documents = journal.retrieve(
                BarbelQueries.effectiveAfter("docid1", LocalDate.of(2021, 12, 1)),
                queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))));
        assertTrue(documents.size() == 0);
    }

}
