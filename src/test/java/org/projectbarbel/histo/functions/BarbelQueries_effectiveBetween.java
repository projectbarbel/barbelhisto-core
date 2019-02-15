package org.projectbarbel.histo.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.projectbarbel.histo.model.EffectivePeriod;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.resultset.ResultSet;

public class BarbelQueries_effectiveBetween {

    private IndexedCollection<DefaultDocument> journal;

    @BeforeEach
    public void setUp() {
        journal = BarbelTestHelper.generateJournalOfDefaultDocuments("docid1",
                Arrays.asList(LocalDate.of(2010, 12, 1), LocalDate.of(2017, 12, 1), LocalDate.of(2020, 1, 1)));
        BarbelHistoContext.getDefaultClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 8, 0, 0));
    }

    @Test
    public void testApply_threeRecord_onePeriodBetween() throws Exception {
        ResultSet<DefaultDocument> documents = journal.retrieve(BarbelQueries.effectiveBetween("docid1",
                EffectivePeriod.of(LocalDate.of(2010, 12, 2), LocalDate.of(2020, 1, 2))));
        assertTrue(documents.size() == 1);
        assertEquals(documents.iterator().next().getBitemporalStamp().getEffectiveTime().from(),
                LocalDate.of(2017, 12, 1));
    }

    @Test
    public void testApply_threeRecord_allBetween() throws Exception {
        ResultSet<DefaultDocument> documents = journal.retrieve(BarbelQueries.effectiveBetween("docid1",
                EffectivePeriod.of(LocalDate.of(2010, 11, 1), BarbelHistoContext.getInfiniteDate())));
        assertTrue(documents.size() == 3);
    }

}
