package com.projectbarbel.histo.journal.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoBuilder;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelMode;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.journal.functions.JournalUpdateStrategyEmbedding.JournalUpdateCase;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalObjectState;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.model.DefaultPojo;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;
import com.projectbarbel.histo.model.Systemclock;

import io.github.benas.randombeans.api.EnhancedRandom;

public class JournalUpdateStrategyEmbedding_PojoMode_Test {

    private DocumentJournal journal;
    private BarbelHistoContext context;

    @Before
    public void setUp() {
        journal = DocumentJournal
                .create(BarbelTestHelper.generateJournalOfDefaultPojos("someId", Arrays.asList(LocalDate.of(2016, 1, 1),
                        LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))), "someId");
        context = BarbelHistoBuilder.barbel().withMode(BarbelMode.POJO)
                .withClock(new Systemclock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0)))
                .withUser("testUser");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testApply_wrongId() throws Exception {
        DefaultDocument doc = new DefaultDocument();
        BarbelHistoContext context = BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL);
        Bitemporal bitemporal = BarbelMode.BITEMPORAL.snapshotMaiden(context, doc,
                BitemporalStamp.createWithDefaultValues());
        new JournalUpdateStrategyEmbedding(context).apply(journal, bitemporal);
    }

    @Test
    public void testApply_POSTOVERLAPPING_UntilIsLocalDateMax() throws Exception {
        
        DefaultPojo doc = new DefaultPojo();
        doc.setDocumentId("someId");
        doc.setData("some data");
        Bitemporal bitemporal = context.getMode().snapshotMaiden(context, doc, BitemporalStamp.createActiveWithContext(
                context, "someId", EffectivePeriod.of(context.getClock().today(), LocalDate.MAX)));
        JournalUpdateStrategyEmbedding function = new JournalUpdateStrategyEmbedding(context);
        List<Object> list = function.apply(journal, bitemporal);
        
        assertTrue(list.size() == 2);
        assertEquals(JournalUpdateCase.POSTOVERLAPPING, function.getActualCase());
        
        assertEquals(((Bitemporal) list.get(0)).getBitemporalStamp().getEffectiveTime().from(),
                context.getClock().today());
        assertEquals(((Bitemporal) list.get(0)).getBitemporalStamp().getEffectiveTime().until(), LocalDate.MAX);
        assertEquals(((Bitemporal) list.get(0)).getBitemporalStamp(), bitemporal.getBitemporalStamp());

        assertEquals(((Bitemporal) list.get(1)).getBitemporalStamp().getEffectiveTime().from(),
                LocalDate.of(2019, 1, 1));
        assertEquals(((Bitemporal) list.get(1)).getBitemporalStamp().getEffectiveTime().until(),
                LocalDate.of(2019, 1, 30));

        assertEquals(((Bitemporal) list.get(1)).getBitemporalStamp().getRecordTime().getCreatedAt(),
                ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()));
        assertEquals(((Bitemporal) list.get(1)).getBitemporalStamp().getRecordTime().getCreatedBy(), "testUser");
        assertEquals(((Bitemporal) list.get(1)).getBitemporalStamp().getRecordTime().getInactivatedAt(),
                RecordPeriod.NOT_INACTIVATED);
        assertEquals(((Bitemporal) list.get(1)).getBitemporalStamp().getRecordTime().getInactivatedBy(),
                RecordPeriod.NOBODY);
        assertEquals(((Bitemporal) list.get(1)).getBitemporalStamp().getRecordTime().getState(),
                BitemporalObjectState.ACTIVE);

        System.out.println(DocumentJournal.prettyPrint(journal.collection(),
                journal.list().get(0).getBitemporalStamp().getDocumentId(), d -> ((DefaultPojo) d).getData()));
    }

    // @formatter:off
    /**
     * A |-------------------|---->|-----------------------> infinite 
     * N                 |-------------| 
     * expecting three inactivated and three new periods 
     * I |-------------------|-----|-----------------------> infinite
     * A |---------------|-------------|-------------------> infinite
     */
    // @formatter:on
    //@Test
    public void testApply_EMBEDDEDOVERLAY() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        Bitemporal bitemporal = context.getMode().snapshotMaiden(context, pojo,
                BitemporalStamp.createWithDefaultValues());
        List<Object> list = new JournalUpdateStrategyEmbedding(context).apply(journal, bitemporal);
        assertTrue(list.size() == 2);
        assertTrue(journal.read().activeVersions().size() == 3);
        assertTrue(journal.read().inactiveVersions().size() == 1);
        System.out.println(DocumentJournal.prettyPrint(journal.collection(),
                journal.list().get(0).getBitemporalStamp().getDocumentId(), d -> ((DefaultDocument) d).getData()));
    }

}
