package com.projectbarbel.histo.functions.journal;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.DefaultDocument;

public class ReaderFunctionGetEffectiveAfterTest {

    @Test
    public void testApply() throws Exception {
        DocumentJournal<DefaultDocument> journal = DocumentJournal.create(BarbelTestHelper.generateJournalOfDefaultValueObjects("docid1", Arrays.asList(LocalDate.of(2010,12,1), LocalDate.of(2017,12,1), LocalDate.of(2100, 1, 1))));
        List<DefaultDocument> current = new ReaderFunctionGetEffectiveAfter<DefaultDocument>().apply(journal, LocalDate.now());
        assertEquals(current.get(0).getEffectiveFrom(), LocalDate.of(2100, 1, 1));
    }

}
