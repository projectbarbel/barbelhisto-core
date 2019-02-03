package com.projectbarbel.histo.functions.journal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.model.EffectivePeriod;

public class ReaderFunctionGetEffectiveBetweenTest {

    private DocumentJournal<DefaultDocument> journal;
    private ReaderFunctionGetEffectiveBetween<DefaultDocument> function;

    @Before
    public void setUp() {
        journal = DocumentJournal.create(BarbelTestHelper.generateJournalOfDefaultValueObjects("docid1",
                Arrays.asList(LocalDate.of(2010, 12, 1), LocalDate.of(2017, 12, 1), LocalDate.of(2020, 1, 1))));
        BarbelHistoContext.getClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 8, 0, 0));
        function = new ReaderFunctionGetEffectiveBetween<DefaultDocument>();
    }

    @Test
    public void testApply_threeRecord_onePeriodBetween() throws Exception {
        List<DefaultDocument> documents = function.apply(journal,
                EffectivePeriod.builder().from(LocalDate.of(2010, 12, 2)).until(LocalDate.of(2020, 1, 2)).build());
        assertTrue(documents.size() == 1);
        assertEquals(documents.get(0).getEffectiveFrom(), LocalDate.of(2017, 12, 1));
    }

    @Test
    public void testApply_threeRecord_allBetween() throws Exception {
        List<DefaultDocument> documents = function.apply(journal,
                EffectivePeriod.builder().from(LocalDate.of(2010, 11, 1)).until(BarbelHistoContext.getInfiniteDate()).build());
        assertTrue(documents.size() == 3);
    }
    
}
