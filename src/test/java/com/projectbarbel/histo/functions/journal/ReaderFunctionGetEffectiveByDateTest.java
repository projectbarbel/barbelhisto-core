package com.projectbarbel.histo.functions.journal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.DefaultDocument;

public class ReaderFunctionGetEffectiveByDateTest {

    private DocumentJournal<DefaultDocument> journal;
    private ReaderFunctionGetEffectiveByDate<DefaultDocument> function;

    @Before
    public void setUp() {
        journal = DocumentJournal.create(BarbelTestHelper.generateJournalOfDefaultValueObjects("docid1",
                Arrays.asList(LocalDate.of(2010, 12, 1), LocalDate.of(2017, 12, 1), LocalDate.of(2020, 1, 1))));
        BarbelHistoContext.instance().clock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 8, 0, 0));
        function = new ReaderFunctionGetEffectiveByDate<DefaultDocument>();
    }

    @Test
    public void testApply() throws Exception {
        Optional<DefaultDocument> document = function.apply(journal, BarbelHistoContext.instance().clock().now().toLocalDate());
        assertTrue(document.isPresent());
        assertEquals(document.get().getEffectiveFrom(), LocalDate.of(2017, 12, 1));
    }

    @Test
    public void testApply_laterDoc() throws Exception {
        Optional<DefaultDocument> document = function.apply(journal, LocalDate.of(2021, 12, 1));
        assertTrue(document.isPresent());
        assertEquals(document.get().getEffectiveFrom(), LocalDate.of(2020, 1, 1));
    }

    @Test
    public void testApply_nonEffective() throws Exception {
        Optional<DefaultDocument> document = function.apply(journal, LocalDate.of(2000, 12, 1));
        assertFalse(document.isPresent());
    }
    
    @Test
    public void testApply_earlierDoc() throws Exception {
        Optional<DefaultDocument> document = function.apply(journal, LocalDate.of(2012, 12, 1));
        assertTrue(document.isPresent());
        assertEquals(document.get().getEffectiveFrom(), LocalDate.of(2010, 12, 1));
    }

}
