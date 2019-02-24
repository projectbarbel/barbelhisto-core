package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.projectbarbel.histo.DocumentJournal.ProcessingState;
import org.projectbarbel.histo.functions.EmbeddingJournalUpdateStrategy;
import org.projectbarbel.histo.functions.EmbeddingJournalUpdateStrategy.JournalUpdateCase;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalObjectState;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.model.RecordPeriod;

import com.googlecode.cqengine.IndexedCollection;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCore_JournalUpdate_Test {

    private DocumentJournal journal;
    private BarbelHistoContext context;

    @AfterAll
    public static void setup() {
        BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
    }

    @Test
    public void testApply_wrongId() throws Exception {
        DefaultDocument doc = new DefaultDocument();
        BarbelHistoContext context = BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL);
        Bitemporal bitemporal = BarbelMode.BITEMPORAL.snapshotMaiden(context, doc, BitemporalStamp.createActive());
        journal = DocumentJournal.create(ProcessingState.INTERNAL, context,
                BarbelTestHelper.generateJournalOfManagedDefaultPojos("someId", Arrays.asList(LocalDate.of(2016, 1, 1),
                        LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))),
                "someId");
        assertThrows(IllegalArgumentException.class,
                () -> new EmbeddingJournalUpdateStrategy(context).accept(journal, bitemporal));
    }

    // @formatter:off
    @SuppressWarnings("unused")
    private static Stream<Arguments> createJournalUpdateCases() {

        // 1.1.2016 1.1.2017 1.1.2018 1.1.2019
        // |------------------|------------------|------------------|---------->
        // Infinite
        // 10:00 Uhr (now)

        return Stream.of(
                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U|---------|
                Arguments.of(LocalDate.of(2015, 7, 1), LocalDate.of(2016, 7, 1), JournalUpdateCase.PREOVERLAPPING, 2,
                        Arrays.asList(LocalDate.of(2015, 7, 1), LocalDate.of(2016, 7, 1), LocalDate.of(2016, 7, 1),
                                LocalDate.of(2017, 1, 1)),
                        1, Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1))),

                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U |------------------------------------>
                Arguments.of(LocalDate.of(2019, 1, 25), LocalDate.MAX, JournalUpdateCase.POSTOVERLAPPING, 2,
                        Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 25), LocalDate.of(2019, 1, 25),
                                LocalDate.MAX),
                        1, Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.MAX)),
                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U |------|
                Arguments.of(LocalDate.of(2018, 7, 1), LocalDate.of(2018, 10, 1), JournalUpdateCase.EMBEDDEDINTERVAL, 3,
                        Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 7, 1), LocalDate.of(2018, 7, 1),
                                LocalDate.of(2018, 10, 1), LocalDate.of(2018, 10, 1), LocalDate.of(2019, 1, 1)),
                        1, Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))),
                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U |--------|
                Arguments.of(LocalDate.of(2017, 10, 1), LocalDate.of(2018, 3, 1), JournalUpdateCase.EMBEDDEDOVERLAP, 3,
                        Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 10, 1), LocalDate.of(2017, 10, 1),
                                LocalDate.of(2018, 3, 1), LocalDate.of(2018, 3, 1), LocalDate.of(2019, 1, 1)),
                        2,
                        Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 1),
                                LocalDate.of(2019, 1, 1))),
                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U |--------------------------------------------------------------------->
                Arguments.of(LocalDate.of(2015, 10, 1), LocalDate.MAX, JournalUpdateCase.OVERLAY, 1,
                        Arrays.asList(LocalDate.of(2015, 10, 1), LocalDate.MAX), 4,
                        Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1), LocalDate.of(2017, 1, 1),
                                LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1),
                                LocalDate.of(2019, 1, 1), LocalDate.MAX)),
                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U |------------------------------------|
                Arguments.of(LocalDate.of(2016, 7, 1), LocalDate.of(2018, 7, 1), JournalUpdateCase.EMBEDDEDOVERLAY, 3,
                        Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 7, 1), LocalDate.of(2016, 7, 1),
                                LocalDate.of(2018, 7, 1), LocalDate.of(2018, 7, 1), LocalDate.of(2019, 1, 1)),
                        3,
                        Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1), LocalDate.of(2017, 1, 1),
                                LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))),
                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U --------------------------------------------|---------------->
                Arguments.of(LocalDate.of(2018, 7, 1), LocalDate.MAX, JournalUpdateCase.POSTOVERLAPPING_OVERLAY, 2,
                        Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 7, 1), LocalDate.of(2018, 7, 1),
                                LocalDate.MAX),
                        2,
                        Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1),
                                LocalDate.MAX)),
                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U |------------------------------|
                Arguments.of(LocalDate.of(2015, 10, 1), LocalDate.of(2017, 3, 1),
                        JournalUpdateCase.PREOVERLAPPING_OVERLAY, 2,
                        Arrays.asList(LocalDate.of(2015, 10, 1), LocalDate.of(2017, 3, 1), LocalDate.of(2017, 3, 1),
                                LocalDate.of(2018, 1, 1), LocalDate.of(2018, 3, 1), LocalDate.of(2019, 1, 1),
                                LocalDate.of(2019, 1, 1), LocalDate.MAX),
                        2, Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1), LocalDate.of(2017, 1, 1),
                                LocalDate.of(2018, 1, 1))));

    }
    // @formatter:on

    // @formatter:off
    @SuppressWarnings("unused")
    private static Stream<Arguments> createJournalEdgeCases() {

        // 1.1.2016 1.1.2017 1.1.2018 1.1.2019
        // |------------------|------------------|------------------|---------->
        // Infinite

        return Stream.of(
                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U|-------|
                Arguments.of(LocalDate.of(2015, 7, 1), LocalDate.of(2016, 1, 1), JournalUpdateCase.STRAIGHTINSERT, 1,
                        Arrays.asList(LocalDate.of(2015, 7, 1), LocalDate.of(2016, 1, 1)), 0, Arrays.asList()),

                // A
                // |------------------|------------------|------------------|----------------------------->
                // Infinite
                // U |-------------------|
                Arguments.of(LocalDate.of(2019, 7, 1), LocalDate.of(2019, 8, 1), JournalUpdateCase.EMBEDDEDINTERVAL, 3,
                        Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 7, 1), LocalDate.of(2019, 7, 1),
                                LocalDate.of(2019, 8, 1), LocalDate.of(2019, 8, 1), LocalDate.MAX),
                        1, Arrays.asList(LocalDate.of(2019, 1, 1), LocalDate.MAX)),
                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U |------------------|
                Arguments.of(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), JournalUpdateCase.EMBEDDEDINTERVAL, 1,
                        Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1)), 1,
                        Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1))),
                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U |--------------|
                Arguments.of(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 10, 1), JournalUpdateCase.EMBEDDEDINTERVAL, 2,
                        Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 10, 1), LocalDate.of(2017, 10, 1),
                                LocalDate.of(2018, 1, 1)),
                        1, Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1))),
                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U |-------------------------------------|
                Arguments.of(LocalDate.of(2017, 1, 1), LocalDate.of(2019, 1, 1), JournalUpdateCase.EMBEDDEDOVERLAY, 1,
                        Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2019, 1, 1)), 2,
                        Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 1),
                                LocalDate.of(2019, 1, 1))),
                // A |------------------|------------------|------------------|------------>
                // Infinite
                // U |--------------------------------------------------------------------->
                // Infinite
                Arguments.of(LocalDate.of(2016, 1, 1), LocalDate.MAX, JournalUpdateCase.POSTOVERLAPPING_OVERLAY, 1,
                        Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.MAX), 4,
                        Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1), LocalDate.of(2017, 1, 1),
                                LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1),
                                LocalDate.of(2019, 1, 1), LocalDate.MAX)));
    }

    // @formatter:on
    // @formatter:off
    @SuppressWarnings("unused")
    private static Stream<Arguments> createJournalDebugCases() {

        // 1.1.2016 1.1.2017 1.1.2018 1.1.2019
        // |------------------|------------------|------------------|---------->
        // Infinite

        return Stream.of(
                // A |------------------|------------------|------------------|---------->
                // Infinite
                // U |------------------|
                Arguments.of(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), JournalUpdateCase.EMBEDDEDINTERVAL, 1,
                        Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1)), 1,
                        Arrays.asList(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1))));

    }
    // @formatter:on

    @ParameterizedTest
    @MethodSource({ "createJournalUpdateCases", "createJournalEdgeCases" })
    public void testCoreSave_Pojo(LocalDate updateFrom, LocalDate updateUntil, JournalUpdateCase updateCase,
            int countOfNewVersions, List<LocalDate> activeEffective, int inactiveCount,
            List<LocalDate> inactiveEffective) throws Exception {
        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0));
        BarbelHistoContext context = BarbelHistoBuilder.barbel().withMode(BarbelMode.POJO).withUser("testUser")
                .withBackboneSupplier(() -> BarbelTestHelper.generateJournalOfManagedDefaultPojos("someId",
                        Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1),
                                LocalDate.of(2019, 1, 1))));
        BarbelHisto<DefaultPojo> core = ((BarbelHistoBuilder) context).build();
        DefaultPojo update = new DefaultPojo();
        update.setDocumentId("someId");
        update.setData("some data");
        core.save(update, updateFrom, updateUntil);
        journal = ((BarbelHistoCore<DefaultPojo>) core).getDocumentJournal("someId");
        if (updateCase.equals(JournalUpdateCase.EMBEDDEDINTERVAL))
            System.out.println(core.prettyPrintJournal("someId"));
        assertEquals(countOfNewVersions, journal.getLastInsert().size());
        assertEquals(updateCase, journal.getLastUpdateCase());
        assertNewVersions(journal.getLastUpdateRequest(), journal.getLastInsert(), activeEffective);
        assertInactivatedVersions(inactiveCount, inactiveEffective);
    }

    @ParameterizedTest
    @MethodSource({ "createJournalUpdateCases", "createJournalEdgeCases" })
    public void testCoreSave_Bitemporal(LocalDate updateFrom, LocalDate updateUntil, JournalUpdateCase updateCase,
            int countOfNewVersions, List<LocalDate> activeEffective, int inactiveCount,
            List<LocalDate> inactiveEffective) throws Exception {
        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0));
        BarbelHistoContext context = BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL).withUser("testUser")
                .withBackboneSupplier(() -> BarbelTestHelper.generateJournalOfDefaultDocuments("someId",
                        Arrays.asList(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1),
                                LocalDate.of(2019, 1, 1))));
        BarbelHisto<DefaultDocument> core = ((BarbelHistoBuilder) context).build();
        DefaultDocument update = new DefaultDocument();
        update.setData("some data");
        update.setId("someId");
        core.save(update, updateFrom, updateUntil);
        journal = ((BarbelHistoCore<DefaultDocument>) core).getDocumentJournal("someId");
        assertEquals(countOfNewVersions, journal.getLastInsert().size());
        assertEquals(updateCase, journal.getLastUpdateCase());
        assertNewVersions(journal.getLastUpdateRequest(), journal.getLastInsert(), activeEffective);
        assertInactivatedVersions(inactiveCount, inactiveEffective);
    }

    @ParameterizedTest
    @MethodSource({ "createJournalUpdateCases", "createJournalEdgeCases" })
    public void testFunctionAccept_Pojo(LocalDate updateFrom, LocalDate updateUntil, JournalUpdateCase updateCase,
            int countOfNewVersions, List<LocalDate> activeEffective, int inactiveCount,
            List<LocalDate> inactiveEffective) throws Exception {
        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0));
        context = BarbelHistoBuilder.barbel().withMode(BarbelMode.POJO).withUser("testUser");
        journal = DocumentJournal.create(ProcessingState.INTERNAL, context,
                BarbelTestHelper.generateJournalOfManagedDefaultPojos("someId", Arrays.asList(LocalDate.of(2016, 1, 1),
                        LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))),
                "someId");
        UpdateReturn updatReturn = performUpdate_Pojo(updateFrom, updateUntil);
        assertTrue(updatReturn.newVersions.size() == countOfNewVersions);
        assertEquals(updateCase, updatReturn.function.getActualCase());
        assertNewVersions(updatReturn.bitemporal, updatReturn.newVersions, activeEffective);
        assertInactivatedVersions(inactiveCount, inactiveEffective);
    }

    @ParameterizedTest
    @MethodSource({ "createJournalUpdateCases", "createJournalEdgeCases" })
    public void testFunctionAccept_Bitemporal(LocalDate updateFrom, LocalDate updateUntil, JournalUpdateCase updateCase,
            int countOfNewVersions, List<LocalDate> activeEffective, int inactiveCount,
            List<LocalDate> inactiveEffective) throws Exception {
        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0));
        context = BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL).withUser("testUser");
        journal = DocumentJournal.create(ProcessingState.INTERNAL, context,
                BarbelTestHelper.generateJournalOfDefaultDocuments("someId", Arrays.asList(LocalDate.of(2016, 1, 1),
                        LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1))),
                "someId");
        UpdateReturn updatReturn = performUpdate_Bitemporal(updateFrom, updateUntil);
        assertTrue(updatReturn.newVersions.size() == countOfNewVersions);
        assertEquals(updateCase, updatReturn.function.getActualCase());
        assertNewVersions(updatReturn.bitemporal, updatReturn.newVersions, activeEffective);
        assertInactivatedVersions(inactiveCount, inactiveEffective);
    }

    @SuppressWarnings("unchecked")
    public <T> void testCoreSave_Flex(BarbelMode mode, LocalDateTime today, IndexedCollection<T> existingJournal,
            LocalDate updateFrom, LocalDate updateUntil, JournalUpdateCase updateCase, int countOfNewVersions,
            List<LocalDate> activeEffective, int inactiveCount, List<LocalDate> inactiveEffective) throws Exception {
        BarbelHistoContext.getBarbelClock().useFixedClockAt(today);
        BarbelHistoContext context = BarbelHistoBuilder.barbel().withMode(mode).withUser("testUser")
                .withBackboneSupplier(() -> existingJournal);
        BarbelHisto<T> core = ((BarbelHistoBuilder) context).build();
        T template = existingJournal.stream().findFirst().get();
        T object = (T) EnhancedRandom.random(template.getClass());
        if (template instanceof BarbelProxy)
            object = (T) EnhancedRandom.random(((BarbelProxy) template).getTarget().getClass());
        core.save(object, updateFrom, updateUntil);
        journal = ((BarbelHistoCore<T>) core).getDocumentJournal("someId");
        assertEquals(countOfNewVersions, journal.getLastInsert().size());
        assertEquals(updateCase, journal.getLastUpdateCase());
        assertNewVersions(journal.getLastUpdateRequest(), journal.getLastInsert(), activeEffective);
        assertInactivatedVersions(inactiveCount, inactiveEffective);
    }

    private void assertInactivatedVersions(int inactiveCount, List<LocalDate> inactiveEffective) {
        List<Bitemporal> inactivated = journal.read().inactiveVersions();
        assertEquals(inactiveCount, inactivated.size());
        for (int i = 0; i < inactivated.size(); i++) {
            assertInactivatedVersion(inactivated.get(i), inactiveEffective.get(i * 2),
                    inactiveEffective.get(i * 2 + 1));
        }
    }

    private UpdateReturn performUpdate_Pojo(LocalDate from, LocalDate until) {
        DefaultPojo update = new DefaultPojo();
        update.setDocumentId("someId");
        update.setData("some data");
        Bitemporal bitemporal = context.getMode().snapshotMaiden(context, update,
                BitemporalStamp.createActive(context, "someId", EffectivePeriod.of(from, until)));
        EmbeddingJournalUpdateStrategy updateStrategy = new EmbeddingJournalUpdateStrategy(context);
        updateStrategy.accept(journal, bitemporal);
        return new UpdateReturn(journal.getLastInsert(), bitemporal, updateStrategy);
    }

    private UpdateReturn performUpdate_Bitemporal(LocalDate from, LocalDate until) {
        DefaultDocument doc = new DefaultDocument();
        Bitemporal bitemporal = BarbelMode.BITEMPORAL.snapshotMaiden(context, doc,
                BitemporalStamp.createActive(context, "someId", EffectivePeriod.of(from, until)));

        EmbeddingJournalUpdateStrategy function = new EmbeddingJournalUpdateStrategy(context);
        function.accept(journal, bitemporal);
        List<Bitemporal> list = journal.getLastInsert();
        return new UpdateReturn(list, bitemporal, function);
    }

    private void assertNewVersions(Bitemporal insertedBitemporal, List<Bitemporal> newVersions,
            List<LocalDate> activeEffective) {

        for (int i = 0; i < newVersions.size(); i++) {
            assertEquals(activeEffective.get(i * 2), newVersions.get(i).getBitemporalStamp().getEffectiveTime().from());
            assertEquals(activeEffective.get(i * 2 + 1),
                    newVersions.get(i).getBitemporalStamp().getEffectiveTime().until());
            assertEquals(ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()),
                    newVersions.get(i).getBitemporalStamp().getRecordTime().getCreatedAt());
            assertEquals("testUser", newVersions.get(i).getBitemporalStamp().getRecordTime().getCreatedBy());
            assertEquals(RecordPeriod.NOT_INACTIVATED,
                    newVersions.get(i).getBitemporalStamp().getRecordTime().getInactivatedAt());
            assertEquals(RecordPeriod.NOBODY,
                    newVersions.get(i).getBitemporalStamp().getRecordTime().getInactivatedBy());
            assertEquals(BitemporalObjectState.ACTIVE,
                    newVersions.get(i).getBitemporalStamp().getRecordTime().getState());
        }

    }

    private void assertInactivatedVersion(Bitemporal inactivated, LocalDate from, LocalDate until) {

        assertEquals(from, inactivated.getBitemporalStamp().getEffectiveTime().from());
        assertEquals(until, inactivated.getBitemporalStamp().getEffectiveTime().until());

        assertEquals(ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()),
                inactivated.getBitemporalStamp().getRecordTime().getCreatedAt());
        assertEquals("SYSTEM", inactivated.getBitemporalStamp().getRecordTime().getCreatedBy());
        assertEquals(ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.systemDefault()),
                inactivated.getBitemporalStamp().getRecordTime().getInactivatedAt());
        assertEquals("testUser", inactivated.getBitemporalStamp().getRecordTime().getInactivatedBy());
        assertEquals(BitemporalObjectState.INACTIVE, inactivated.getBitemporalStamp().getRecordTime().getState());

    }

    private static class UpdateReturn {
        public List<Bitemporal> newVersions;
        public Bitemporal bitemporal;
        public EmbeddingJournalUpdateStrategy function;

        public UpdateReturn(List<Bitemporal> newVersions, Bitemporal bitemporal,
                EmbeddingJournalUpdateStrategy function) {
            super();
            this.newVersions = newVersions;
            this.bitemporal = bitemporal;
            this.function = function;
        }
    }

}
