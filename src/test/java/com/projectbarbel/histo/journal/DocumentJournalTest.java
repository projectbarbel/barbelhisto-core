package com.projectbarbel.histo.journal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.Test;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.BarbelHistoBuilder;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelMode;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.journal.functions.JournalUpdateStrategyEmbedding;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.model.Bitemporal;

public class DocumentJournalTest {

    @Test
    public void testCreate_withList() {
        DocumentJournal journal = DocumentJournal
                .create(BarbelTestHelper.generateJournalOfDefaultValueObjects("#12345",
                        Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 4, 1))), "#12345");
        assertEquals(2, journal.size());
    }

    @Test
    public void testCreate_withList_differentDocumentIds() throws Exception {
        DocumentJournal journal = DocumentJournal
                .create(BarbelTestHelper.asIndexedCollection(BarbelTestHelper.random(DefaultDocument.class),
                        BarbelTestHelper.random(DefaultDocument.class)), "arbitrary");
        assertTrue(journal.size() == 0);
    }

    @Test
    public void testCreate_withList_Empty() throws Exception {
        IndexedCollection<Object> list = new ConcurrentIndexedCollection<Object>();
        DocumentJournal journal = DocumentJournal.create(list, "");
        assertNotNull(journal);
    }

    @Test(expected = NullPointerException.class)
    public void testCreate_withList_null() throws Exception {
        IndexedCollection<Object> list = null;
        DocumentJournal.create(list, "");
    }

    @Test
    public void testPrettyPrint() throws Exception {
        assertNotNull(DocumentJournal.prettyPrint(BarbelTestHelper.asIndexedCollection(BarbelTestHelper.random(DefaultDocument.class),
                BarbelTestHelper.random(DefaultDocument.class)), "arbitrary"));
    }

    @Test
    public void testUpdate() throws Exception {
        IndexedCollection<Object> coll = new ConcurrentIndexedCollection<Object>();
        BarbelHistoContext.getClock().useFixedClockAt(LocalDateTime.of(2019, 2, 1, 8, 0));
        DefaultDocument doc = DefaultDocument.builder().withData("some data")
                .withBitemporalStamp(BitemporalStamp.defaultValues()).build();
        coll.add(doc);
        DocumentJournal journal = DocumentJournal.create(coll, doc.getBitemporalStamp().getDocumentId());
        VersionUpdateResult update = BarbelHistoFactory.createDefaultVersionUpdate(BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL), doc).prepare()
                .effectiveFrom(BarbelHistoContext.getClock().now().plusDays(1).toLocalDate()).execute();
        journal.update(new JournalUpdateStrategyEmbedding(BarbelHistoBuilder.barbel()), update);
        System.out.println(DocumentJournal.prettyPrint(journal.collection(), doc.getBitemporalStamp().getDocumentId()));
    }

    @Test
    public void testList() throws Exception {
        DocumentJournal journal = DocumentJournal
                .create(BarbelTestHelper.generateJournalOfDefaultValueObjects("#12345",
                        Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 4, 1))), "#12345");
        assertEquals(((Bitemporal)journal.list().get(0)).getBitemporalStamp().getEffectiveTime().from(), LocalDate.of(2019, 1, 1));
    }

}
