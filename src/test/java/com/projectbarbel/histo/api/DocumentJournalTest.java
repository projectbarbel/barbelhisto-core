package com.projectbarbel.histo.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.api.VersionUpdate.VersionUpdateResult;
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
    public void testCreate_withList_differentDocumentIds() throws Exception {
        DocumentJournal.create(Arrays.asList(BarbelTestHelper.random(DefaultDocument.class),
                BarbelTestHelper.random(DefaultDocument.class)));
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
                .create(DefaultDocument.builder().withBitemporalStamp(stamp).withData("some initial data").build());
        assertTrue(journal.size() == 1);
        assertTrue(journal.list().get(0).getBitemporalStamp() != null);
    }

    @Test
    public void testCreate_withInitialDocument_withoutStamp() throws Exception {
        DocumentJournal<DefaultDocument> journal = DocumentJournal
                .create(DefaultDocument.builder().withData("some initial data").build());
        assertTrue(journal.size() == 1);
        assertTrue(journal.list().get(0).getBitemporalStamp() != null);
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

    @Test
    public void testPrettyPrint() throws Exception {
        DocumentJournal<DefaultDocument> journal = DocumentJournal
                .create(DefaultDocument.builder().withData("some initial data").build());
        assertNotNull(journal.prettyPrint());
    }

    @Test
    public void testInsert() throws Exception {
        BarbelHistoContext.instance().clock().useFixedClockAt(LocalDateTime.of(2019, 2, 1, 8, 0));
        DefaultDocument doc = DefaultDocument.builder().withData("some data").build();
        DocumentJournal<DefaultDocument> journal = DocumentJournal.create(doc);
        VersionUpdateResult<DefaultDocument> update = VersionUpdate.of(doc).prepare()
                .from(BarbelHistoContext.instance().clock().now().plusDays(1).toLocalDate()).execute();
        journal.add(update::newPrecedingVersion);
        System.out.println(journal.prettyPrint());
    }

}
