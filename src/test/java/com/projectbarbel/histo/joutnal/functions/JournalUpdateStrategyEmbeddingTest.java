package com.projectbarbel.histo.joutnal.functions;

import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.journal.VersionUpdate;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.joutnal.functions.JournalUpdateStrategyEmbedding;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.model.Systemclock;

public class JournalUpdateStrategyEmbeddingTest {

    private DocumentJournal<DefaultDocument> journal;
    private Systemclock clock = new Systemclock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0));

    @Before
    public void setUp() {
        journal = DocumentJournal.create(BarbelTestHelper.generateJournalOfDefaultValueObjects("someId", Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))), "someId");
    }
    
    @Test
    public void testApply_lastIntervall() throws Exception {
        VersionUpdate<DefaultDocument> update = VersionUpdate.of(journal.list().get(3)).prepare().effectiveFrom(clock.now().toLocalDate()).untilInfinite().setProperty("data", "some new data").get();
        VersionUpdateResult<DefaultDocument> result = update.execute();
        List<DefaultDocument> list = new JournalUpdateStrategyEmbedding<DefaultDocument>().apply(journal, result);
        assertTrue(list.size()==2);
        assertTrue(journal.read().activeVersions().size()==3);
        assertTrue(journal.read().inactiveVersions().size()==1);        
        System.out.println(journal.prettyPrint());
    }

}
