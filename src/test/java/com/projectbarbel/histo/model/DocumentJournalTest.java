package com.projectbarbel.histo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.model.DocumentJournal;

public class DocumentJournalTest {

    @Test
    public void testCreate_withList() {
        DocumentJournal journal = DocumentJournal
                .create(BarbelTestHelper.generateJournalOfDefaultDocuments("#12345",
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

    @Test
    public void testCreate_withList_null() throws Exception {
        IndexedCollection<Object> list = null;
        assertThrows(NullPointerException.class, () -> DocumentJournal.create(list, ""));
    }

    @Test
    public void testUpdate() throws Exception {
        IndexedCollection<Object> coll = new ConcurrentIndexedCollection<Object>();
        BarbelHistoContext.getDefaultClock().useFixedClockAt(LocalDateTime.of(2019, 2, 1, 8, 0));
        DefaultDocument doc = DefaultDocument.builder().withData("some data")
                .withBitemporalStamp(BitemporalStamp.createActive()).build();
        coll.add(doc);
        DocumentJournal journal = DocumentJournal.create(coll, doc.getBitemporalStamp().getDocumentId());
        journal.accept(Arrays.asList(doc));
        assertTrue(journal.list().size()==1);
    }

    @Test
    public void testList() throws Exception {
        DocumentJournal journal = DocumentJournal
                .create(BarbelTestHelper.generateJournalOfDefaultDocuments("#12345",
                        Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 4, 1))), "#12345");
        assertEquals(((Bitemporal)journal.list().get(0)).getBitemporalStamp().getEffectiveTime().from(), LocalDate.of(2019, 1, 1));
    }

}
