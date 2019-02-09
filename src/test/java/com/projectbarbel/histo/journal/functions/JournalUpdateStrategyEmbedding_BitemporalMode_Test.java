package com.projectbarbel.histo.journal.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;
import com.projectbarbel.histo.model.Systemclock;

public class JournalUpdateStrategyEmbedding_BitemporalMode_Test {

    private DocumentJournal journal;
    private BarbelHistoContext context;

    @Before
    public void setUp() {
        journal = DocumentJournal.create(
                BarbelTestHelper.generateJournalOfDefaultDocuments("someId", Arrays.asList(LocalDate.of(2016, 1, 1),
                        LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))),
                "someId");
        context = BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL)
                .withClock(new Systemclock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0))).withUser("testUser");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testApply_wrongId() throws Exception {
        DefaultDocument doc = new DefaultDocument();
        Bitemporal bitemporal = BarbelMode.BITEMPORAL.snapshotMaiden(context, doc,
                BitemporalStamp.createWithDefaultValues());
        new JournalUpdateStrategyEmbedding(context).accept(journal, bitemporal);
    }

    @Test
    public void testApply_POSTOVERLAPPING_UntilIsLocalDateMax() throws Exception {
        UpdateReturn returnValues = performUpdate(context.getClock().today(), LocalDate.MAX);
        assertTrue(returnValues.newVersions.size() == 2);
        assertEquals(JournalUpdateCase.POSTOVERLAPPING, returnValues.function.getActualCase());
        assertTwoNewVersions((Bitemporal) returnValues.newVersions.get(0), context.getClock().today(), LocalDate.MAX,
                (Bitemporal) returnValues.newVersions.get(1), LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 30),
                returnValues.bitemporal);
        List<Bitemporal> inactivated = journal.read().inactiveVersions();
        assertEquals(1, inactivated.size());
        assertInactivatedVersion(inactivated.get(0), LocalDate.of(2019, 1, 1), LocalDate.MAX);
        System.out.println(DocumentJournal.prettyPrint(journal.collection(),
                journal.list().get(0).getBitemporalStamp().getDocumentId(), d -> ((DefaultDocument) d).getData()));
    }

    private UpdateReturn performUpdate(LocalDate from, LocalDate until) {
        DefaultDocument doc = new DefaultDocument();
        Bitemporal bitemporal = BarbelMode.BITEMPORAL.snapshotMaiden(context, doc, BitemporalStamp.createActiveWithContext(
                context, "someId", EffectivePeriod.of(context.getClock().today(), LocalDate.MAX)));

        JournalUpdateStrategyEmbedding function = new JournalUpdateStrategyEmbedding(context);
        function.accept(journal, bitemporal);
        List<Bitemporal> list = journal.getLastInsert();
        return new UpdateReturn(list, bitemporal, function);
    }

    private void assertTwoNewVersions(Bitemporal first, LocalDate from1, LocalDate until1, Bitemporal second,
            LocalDate from2, LocalDate until2, Bitemporal update) {

        assertEquals(first.getBitemporalStamp().getEffectiveTime().from(), from1);
        assertEquals(first.getBitemporalStamp().getEffectiveTime().until(), until1);
        assertEquals(first.getBitemporalStamp(), update.getBitemporalStamp());

        assertEquals(second.getBitemporalStamp().getEffectiveTime().from(), from2);
        assertEquals(second.getBitemporalStamp().getEffectiveTime().until(), until2);

        assertEquals(first.getBitemporalStamp().getRecordTime().getCreatedAt(),
                ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()));
        assertEquals(first.getBitemporalStamp().getRecordTime().getCreatedBy(), "testUser");
        assertEquals(first.getBitemporalStamp().getRecordTime().getInactivatedAt(), RecordPeriod.NOT_INACTIVATED);
        assertEquals(first.getBitemporalStamp().getRecordTime().getInactivatedBy(), RecordPeriod.NOBODY);
        assertEquals(first.getBitemporalStamp().getRecordTime().getState(), BitemporalObjectState.ACTIVE);

        assertEquals(second.getBitemporalStamp().getRecordTime().getCreatedAt(),
                ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()));
        assertEquals(second.getBitemporalStamp().getRecordTime().getCreatedBy(), "testUser");
        assertEquals(second.getBitemporalStamp().getRecordTime().getInactivatedAt(), RecordPeriod.NOT_INACTIVATED);
        assertEquals(second.getBitemporalStamp().getRecordTime().getInactivatedBy(), RecordPeriod.NOBODY);
        assertEquals(second.getBitemporalStamp().getRecordTime().getState(), BitemporalObjectState.ACTIVE);

    }

    private void assertInactivatedVersion(Bitemporal inactivated, LocalDate from, LocalDate until) {

        assertEquals(inactivated.getBitemporalStamp().getEffectiveTime().from(), from);
        assertEquals(inactivated.getBitemporalStamp().getEffectiveTime().until(), until);

        assertNotEquals(inactivated.getBitemporalStamp().getRecordTime().getCreatedAt(),
                ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()));
        assertEquals(inactivated.getBitemporalStamp().getRecordTime().getCreatedBy(), "SYSTEM");
        assertEquals(inactivated.getBitemporalStamp().getRecordTime().getInactivatedAt(),
                ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()));
        assertEquals(inactivated.getBitemporalStamp().getRecordTime().getInactivatedBy(), "testUser");
        assertEquals(inactivated.getBitemporalStamp().getRecordTime().getState(), BitemporalObjectState.INACTIVE);

    }

    public static class UpdateReturn {
        public List<Bitemporal> newVersions;
        public Bitemporal bitemporal;
        public JournalUpdateStrategyEmbedding function;

        public UpdateReturn(List<Bitemporal> newVersions, Bitemporal bitemporal,
                JournalUpdateStrategyEmbedding function) {
            super();
            this.newVersions = newVersions;
            this.bitemporal = bitemporal;
            this.function = function;
        }
    }

}
