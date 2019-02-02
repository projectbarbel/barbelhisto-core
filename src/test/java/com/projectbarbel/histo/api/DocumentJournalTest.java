package com.projectbarbel.histo.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultDocument;

public class DocumentJournalTest {

    @Test
    public void testCreate_withList() {
        DocumentJournal<DefaultDocument> journal = DocumentJournal
                .create(BarbelTestHelper.generateJournalOfDefaultValueObjects("#12345",
                        Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 4, 1))));
        assertTrue(journal.size() == 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_withList_Empty() throws Exception {
        List<DefaultDocument> list = Collections.emptyList();
        DocumentJournal.create(list);
    }

    @Test(expected = NullPointerException.class)
    public void testCreate_withList_null() throws Exception {
        List<DefaultDocument> list = null;
        DocumentJournal.create(list);
    }

    @Test
    public void testCreate_withDocument() throws Exception {
        DocumentJournal<DefaultDocument> journal = DocumentJournal
                .create(BarbelTestHelper.random(DefaultDocument.class));
        assertTrue(journal.size() == 1);
    }

    @Test
    public void testCreate_withInitialDocument() throws Exception {
        BitemporalStamp stamp = BitemporalStamp.initial();
        DocumentJournal<DefaultDocument> journal = DocumentJournal
                .create(BarbelTestHelper.random(DefaultDocument.class));
        assertTrue(journal.size() == 1);
    }
    
    @Test(expected = NullPointerException.class)
    public void testCreate_withDocument_null() throws Exception {
        DefaultDocument dflt = null;
        DocumentJournal.create(dflt);
    }

    @Test
    public void testSortAscendingByEffectiveDate() throws Exception {
        DocumentJournal<DefaultDocument> journal = DocumentJournal
                .create(BarbelTestHelper.generateJournalOfDefaultValueObjects("#12345",
                        Arrays.asList(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 8, 1), LocalDate.of(2019, 1, 1))));
        assertFalse(journal.sortAscendingByEffectiveDate().list().get(1).getEffectiveFrom()
                .isAfter(journal.list().get(2).getEffectiveFrom()));
        assertTrue(journal.list().get(0).getEffectiveFrom().equals(LocalDate.of(2019, 1, 1)));
        assertTrue(journal.list().get(1).getEffectiveFrom().equals(LocalDate.of(2019, 4, 1)));
        assertTrue(journal.list().get(2).getEffectiveFrom().equals(LocalDate.of(2019, 8, 1)));
    }

}
