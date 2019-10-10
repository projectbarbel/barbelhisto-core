package org.projectbarbel.histo.suite.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.BarbelTestHelper;
import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.DocumentJournal.ProcessingState;
import org.projectbarbel.histo.functions.EmbeddingJournalUpdateStrategy;
import org.projectbarbel.histo.functions.EmbeddingJournalUpdateStrategy.JournalUpdateCase;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalObjectState;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalUpdate;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.model.RecordPeriod;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTTestStandard;

@ExtendWith(BTTestStandard.class)
public class BarbelHistoCore_JournalUpdate_SuiteTest {

    private BarbelHistoContext context;

    @BeforeAll
    public static void beforeAll() {
        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, 1, 30, 10, 0).atZone(ZoneId.of("Z")));
    }

    @AfterAll
    public static void setup() {
        BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
    }

    @BeforeEach
    public void setUp() {
        BTExecutionContext.INSTANCE.clearResources();
    }

    @Test
    public void testApply_wrongId() throws Exception {
        DefaultDocument doc = new DefaultDocument();
        BarbelHistoContext context = BTExecutionContext.INSTANCE.barbel(DefaultDocument.class)
                .withMode(BarbelMode.BITEMPORAL);
        Bitemporal bitemporal = BarbelMode.BITEMPORAL.snapshotMaiden(context, doc, BitemporalStamp.createActive());
        DocumentJournal journal = DocumentJournal.create(ProcessingState.INTERNAL, context,
                BarbelTestHelper.generateJournalOfManagedDefaultPojos(
                        BTExecutionContext.INSTANCE.barbel(DefaultDocument.class), "someId",
                        Arrays.asList(ZonedDateTime.parse("2016-01-01T00:00:00Z"), ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse("2018-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2019-01-01T00:00:00Z"))),
                "someId");
        assertThrows(IllegalArgumentException.class,
                () -> new EmbeddingJournalUpdateStrategy(context).accept(journal, bitemporal));
    }

    // @formatter:off
    @SuppressWarnings("unused")
    private static Stream<Arguments> createJournalUpdateCases() {

        //            1.1.2016           1.1.2017           1.1.2018           1.1.2019
        //               |------------------|------------------|------------------|---------------------> Infinite 
        //                                                                              | 30.1.2019 10:00 Uhr (now)

        return Stream.of(
                // A     |------------------|------------------|------------------|----------> Infinite
                // U |-----------|
                Arguments.of(ZonedDateTime.parse("2015-07-01T00:00:00Z"), ZonedDateTime.parse("2016-07-01T00:00:00Z"), JournalUpdateCase.PREOVERLAPPING, 2,
                        Arrays.asList(ZonedDateTime.parse("2015-07-01T00:00:00Z"), ZonedDateTime.parse(
                                "2016-07-01T00:00:00Z"), ZonedDateTime.parse("2016-07-01T00:00:00Z"),
                                ZonedDateTime.parse("2017-01-01T00:00:00Z")),
                        1, Arrays.asList(ZonedDateTime.parse("2016-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2017-01-01T00:00:00Z"))),

                //     A |------------------|------------------|------------------|--------------------->
                //                                                                   U |---------------->
                Arguments.of(ZonedDateTime.parse("2019-01-25T00:00:00Z"), EffectivePeriod.INFINITE, JournalUpdateCase.POSTOVERLAPPING, 2,
                        Arrays.asList(ZonedDateTime.parse("2019-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2019-01-25T00:00:00Z"), ZonedDateTime.parse("2019-01-25T00:00:00Z"),
                                EffectivePeriod.INFINITE),
                        1, Arrays.asList(ZonedDateTime.parse("2019-01-01T00:00:00Z"), EffectivePeriod.INFINITE)),
                //     A |------------------|------------------|------------------|---------->
                //                                                    U |------|
                Arguments.of(ZonedDateTime.parse("2018-07-01T00:00:00Z"), ZonedDateTime.parse("2018-10-01T00:00:00Z"), JournalUpdateCase.EMBEDDEDINTERVAL, 3,
                        Arrays.asList(ZonedDateTime.parse("2018-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2018-07-01T00:00:00Z"), ZonedDateTime.parse("2018-07-01T00:00:00Z"),
                                ZonedDateTime.parse("2018-10-01T00:00:00Z"), ZonedDateTime.parse(
                                        "2018-10-01T00:00:00Z"), ZonedDateTime.parse("2019-01-01T00:00:00Z")),
                        1, Arrays.asList(ZonedDateTime.parse("2018-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2019-01-01T00:00:00Z"))),
                //     A |------------------|------------------|------------------|---------->
                //                                      U |--------|
                Arguments.of(ZonedDateTime.parse("2017-10-01T00:00:00Z"), ZonedDateTime.parse("2018-03-01T00:00:00Z"), JournalUpdateCase.EMBEDDEDOVERLAP, 3,
                        Arrays.asList(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2017-10-01T00:00:00Z"), ZonedDateTime.parse("2017-10-01T00:00:00Z"),
                                ZonedDateTime.parse("2018-03-01T00:00:00Z"), ZonedDateTime.parse(
                                        "2018-03-01T00:00:00Z"), ZonedDateTime.parse("2019-01-01T00:00:00Z")),
                        2,
                        Arrays.asList(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2018-01-01T00:00:00Z"), ZonedDateTime.parse("2018-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2019-01-01T00:00:00Z"))),
                //     A |------------------|------------------|------------------|--------------> Infinite
                // U |---------------------------------------------------------------------------> Infinite
                Arguments.of(ZonedDateTime.parse("2015-10-01T00:00:00Z"), EffectivePeriod.INFINITE, JournalUpdateCase.OVERLAY, 1,
                        Arrays.asList(ZonedDateTime.parse("2015-10-01T00:00:00Z"), EffectivePeriod.INFINITE), 4,
                        Arrays.asList(ZonedDateTime.parse("2016-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2017-01-01T00:00:00Z"), ZonedDateTime.parse("2017-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2018-01-01T00:00:00Z"), ZonedDateTime.parse(
                                        "2018-01-01T00:00:00Z"), ZonedDateTime.parse("2019-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2019-01-01T00:00:00Z"), EffectivePeriod.INFINITE)),
                //    A |------------------|------------------|------------------|---------->
                //             U |--------------------------------------|
                Arguments.of(ZonedDateTime.parse("2016-07-01T00:00:00Z"), ZonedDateTime.parse("2018-07-01T00:00:00Z"), JournalUpdateCase.EMBEDDEDOVERLAY, 3,
                        Arrays.asList(ZonedDateTime.parse("2016-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2016-07-01T00:00:00Z"), ZonedDateTime.parse("2016-07-01T00:00:00Z"),
                                ZonedDateTime.parse("2018-07-01T00:00:00Z"), ZonedDateTime.parse(
                                        "2018-07-01T00:00:00Z"), ZonedDateTime.parse("2019-01-01T00:00:00Z")),
                        3,
                        Arrays.asList(ZonedDateTime.parse("2016-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2017-01-01T00:00:00Z"), ZonedDateTime.parse("2017-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2018-01-01T00:00:00Z"), ZonedDateTime.parse(
                                        "2018-01-01T00:00:00Z"), ZonedDateTime.parse("2019-01-01T00:00:00Z"))),
                //   A |------------------|------------------|------------------|---------->
                //                                                U|----------------------->
                Arguments.of(ZonedDateTime.parse("2018-07-01T00:00:00Z"), EffectivePeriod.INFINITE, JournalUpdateCase.POSTOVERLAPPING_OVERLAY, 2,
                        Arrays.asList(ZonedDateTime.parse("2018-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2018-07-01T00:00:00Z"), ZonedDateTime.parse("2018-07-01T00:00:00Z"),
                                EffectivePeriod.INFINITE),
                        2,
                        Arrays.asList(ZonedDateTime.parse("2018-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2019-01-01T00:00:00Z"), ZonedDateTime.parse("2019-01-01T00:00:00Z"),
                                EffectivePeriod.INFINITE)),
                //   A |------------------|------------------|------------------|---------->
                // U |------------------------------|
                Arguments.of(ZonedDateTime.parse("2015-10-01T00:00:00Z"), ZonedDateTime.parse("2017-03-01T00:00:00Z"),
                        JournalUpdateCase.PREOVERLAPPING_OVERLAY, 2,
                        Arrays.asList(ZonedDateTime.parse("2015-10-01T00:00:00Z"), ZonedDateTime.parse(
                                "2017-03-01T00:00:00Z"), ZonedDateTime.parse("2017-03-01T00:00:00Z"),
                                ZonedDateTime.parse("2018-01-01T00:00:00Z"), ZonedDateTime.parse(
                                        "2018-03-01T00:00:00Z"), ZonedDateTime.parse("2019-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2019-01-01T00:00:00Z"), EffectivePeriod.INFINITE),
                        2, Arrays.asList(ZonedDateTime.parse("2016-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2017-01-01T00:00:00Z"), ZonedDateTime.parse("2017-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2018-01-01T00:00:00Z"))));

    }
    // @formatter:on

    // @formatter:off
    @SuppressWarnings("unused")
    private static Stream<Arguments> createJournalEdgeCases() {

        //           1.1.2016           1.1.2017           1.1.2018           1.1.2019
        //              |------------------|------------------|------------------|----------> Infinite

        return Stream.of(
                //    A |------------------|------------------|------------------|---------->
                // U|---|
                Arguments.of(ZonedDateTime.parse("2015-07-01T00:00:00Z"), ZonedDateTime.parse("2016-01-01T00:00:00Z"), JournalUpdateCase.STRAIGHTINSERT, 1,
                        Arrays.asList(ZonedDateTime.parse("2015-07-01T00:00:00Z"), ZonedDateTime.parse(
                                "2016-01-01T00:00:00Z")), 0, Arrays.asList()),

                //    A |------------------|------------------|------------------|----------------------------->
                //                                                                   U |-------|
                Arguments.of(ZonedDateTime.parse("2019-07-01T00:00:00Z"), ZonedDateTime.parse("2019-08-01T00:00:00Z"), JournalUpdateCase.EMBEDDEDINTERVAL, 3,
                        Arrays.asList(ZonedDateTime.parse("2019-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2019-07-01T00:00:00Z"), ZonedDateTime.parse("2019-07-01T00:00:00Z"),
                                ZonedDateTime.parse("2019-08-01T00:00:00Z"), ZonedDateTime.parse(
                                        "2019-08-01T00:00:00Z"), EffectivePeriod.INFINITE),
                        1, Arrays.asList(ZonedDateTime.parse("2019-01-01T00:00:00Z"), EffectivePeriod.INFINITE)),
                //    A |------------------|------------------|------------------|---------->
                //                       U |------------------|
                Arguments.of(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse("2018-01-01T00:00:00Z"), JournalUpdateCase.EMBEDDEDINTERVAL, 1,
                        Arrays.asList(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2018-01-01T00:00:00Z")), 1,
                        Arrays.asList(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2018-01-01T00:00:00Z"))),
                //    A |------------------|------------------|------------------|---------->
                //                       U |--------------|
                Arguments.of(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse("2017-10-01T00:00:00Z"), JournalUpdateCase.EMBEDDEDINTERVAL, 2,
                        Arrays.asList(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2017-10-01T00:00:00Z"), ZonedDateTime.parse("2017-10-01T00:00:00Z"),
                                ZonedDateTime.parse("2018-01-01T00:00:00Z")),
                        1, Arrays.asList(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2018-01-01T00:00:00Z"))),
                //    A |------------------|------------------|------------------|---------->
                //                       U |-------------------------------------|
                Arguments.of(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse("2019-01-01T00:00:00Z"), JournalUpdateCase.EMBEDDEDOVERLAY, 1,
                        Arrays.asList(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2019-01-01T00:00:00Z")), 2,
                        Arrays.asList(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2018-01-01T00:00:00Z"), ZonedDateTime.parse("2018-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2019-01-01T00:00:00Z"))),
                //    A |------------------|------------------|------------------|------------>
                //    U |--------------------------------------------------------------------->
                Arguments.of(ZonedDateTime.parse("2016-01-01T00:00:00Z"), EffectivePeriod.INFINITE, JournalUpdateCase.POSTOVERLAPPING_OVERLAY, 1,
                        Arrays.asList(ZonedDateTime.parse("2016-01-01T00:00:00Z"), EffectivePeriod.INFINITE), 4,
                        Arrays.asList(ZonedDateTime.parse("2016-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2017-01-01T00:00:00Z"), ZonedDateTime.parse("2017-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2018-01-01T00:00:00Z"), ZonedDateTime.parse(
                                        "2018-01-01T00:00:00Z"), ZonedDateTime.parse("2019-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2019-01-01T00:00:00Z"), EffectivePeriod.INFINITE)));
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
                // U |------------------|
                Arguments.of(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse("2018-01-01T00:00:00Z"), JournalUpdateCase.EMBEDDEDINTERVAL, 1,
                        Arrays.asList(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2018-01-01T00:00:00Z")), 1,
                        Arrays.asList(ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2018-01-01T00:00:00Z"))));

    }
    // @formatter:on

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource({ "createJournalUpdateCases", "createJournalEdgeCases" })
    public void testCoreSave_Pojo(ZonedDateTime updateFrom, ZonedDateTime updateUntil, JournalUpdateCase updateCase,
            int countOfNewVersions, List<ZonedDateTime> activeEffective, int inactiveCount,
            List<ZonedDateTime> inactiveEffective) throws Exception {
        BarbelHistoContext context = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).withMode(BarbelMode.POJO)
                .withUser("testUser");
        BarbelHisto<DefaultPojo> core = ((BarbelHistoBuilder) context).build();
        DefaultPojo pojo = new DefaultPojo("someId", "some initial");
        core.save(pojo, ZonedDateTime.parse("2016-01-01T00:00:00Z"), ZonedDateTime.parse("2017-01-01T00:00:00Z"));
        core.save(pojo, ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse("2018-01-01T00:00:00Z"));
        core.save(pojo, ZonedDateTime.parse("2018-01-01T00:00:00Z"), ZonedDateTime.parse("2019-01-01T00:00:00Z"));
        core.save(pojo, ZonedDateTime.parse("2019-01-01T00:00:00Z"), EffectivePeriod.INFINITE);
        DefaultPojo update = new DefaultPojo();
        update.setDocumentId("someId");
        update.setData("some data");
        @SuppressWarnings("rawtypes")
        BitemporalUpdate bitemporalUpdate = core.save(update, updateFrom, updateUntil);
        assertEquals(countOfNewVersions, bitemporalUpdate.getInserts().size());
        assertEquals(updateCase, bitemporalUpdate.getUpdateCase());
        assertNewVersions((Bitemporal) bitemporalUpdate.getUpdateRequest(), bitemporalUpdate.getInserts(),
                activeEffective);
        bitemporalUpdate.getInactivations().sort((v1, v2) -> ((Bitemporal) v1).getBitemporalStamp().getEffectiveTime()
                .until().isBefore(((Bitemporal) v2).getBitemporalStamp().getEffectiveTime().until()) ? -1 : 1);
        assertInactivatedVersions(inactiveCount, inactiveEffective, bitemporalUpdate.getInactivations());
    }

    @ParameterizedTest
    @MethodSource({ "createJournalUpdateCases", "createJournalEdgeCases" })
    public void testCoreSave_Bitemporal(ZonedDateTime updateFrom, ZonedDateTime updateUntil, JournalUpdateCase updateCase,
            int countOfNewVersions, List<ZonedDateTime> activeEffective, int inactiveCount,
            List<ZonedDateTime> inactiveEffective) throws Exception {
        BarbelHistoContext context = BTExecutionContext.INSTANCE.barbel(DefaultDocument.class)
                .withMode(BarbelMode.BITEMPORAL).withUser("testUser");
        BarbelHisto<DefaultDocument> core = ((BarbelHistoBuilder) context).build();
        DefaultDocument pojo = new DefaultDocument("someId", BitemporalStamp.createActive(), "some initial");
        core.save(pojo, ZonedDateTime.parse("2016-01-01T00:00:00Z"), ZonedDateTime.parse("2017-01-01T00:00:00Z"));
        core.save(pojo, ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse("2018-01-01T00:00:00Z"));
        core.save(pojo, ZonedDateTime.parse("2018-01-01T00:00:00Z"), ZonedDateTime.parse("2019-01-01T00:00:00Z"));
        core.save(pojo, ZonedDateTime.parse("2019-01-01T00:00:00Z"), EffectivePeriod.INFINITE);
        DefaultDocument update = new DefaultDocument();
        update.setData("some data");
        update.setId("someId");
        BitemporalUpdate<? extends Bitemporal> bitemporalUpdate = core.save(update, updateFrom, updateUntil);
        assertEquals(countOfNewVersions, bitemporalUpdate.getInserts().size());
        assertEquals(updateCase, bitemporalUpdate.getUpdateCase());
        assertNewVersions(bitemporalUpdate.getUpdateRequest(), bitemporalUpdate.getInserts(), activeEffective);
        bitemporalUpdate.getInactivations().sort((v1, v2) -> v1.getBitemporalStamp().getEffectiveTime().until()
                .isBefore(v2.getBitemporalStamp().getEffectiveTime().until()) ? -1 : 1);
        assertInactivatedVersions(inactiveCount, inactiveEffective, bitemporalUpdate.getInactivations());
    }

    @ParameterizedTest
    @MethodSource({ "createJournalUpdateCases", "createJournalEdgeCases" })
    public void testFunctionAccept_Pojo(ZonedDateTime updateFrom, ZonedDateTime updateUntil, JournalUpdateCase updateCase,
            int countOfNewVersions, List<ZonedDateTime> activeEffective, int inactiveCount,
            List<ZonedDateTime> inactiveEffective) throws Exception {
        context = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).withMode(BarbelMode.POJO).withUser("testUser");
        DocumentJournal journal = DocumentJournal.create(ProcessingState.INTERNAL, context,
                BarbelTestHelper.generateJournalOfManagedDefaultPojos(
                        BTExecutionContext.INSTANCE.barbel(DefaultPojo.class), "someId",
                        Arrays.asList(ZonedDateTime.parse("2016-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2017-01-01T00:00:00Z"), ZonedDateTime.parse("2018-01-01T00:00:00Z"),
                                ZonedDateTime.parse("2019-01-01T00:00:00Z"))),
                "someId");
        UpdateReturn updatReturn = performUpdate_Pojo(updateFrom, updateUntil, journal);
        assertTrue(updatReturn.newVersions.size() == countOfNewVersions);
        assertEquals(updateCase, updatReturn.function.getActualCase());
        assertNewVersions(updatReturn.bitemporal, updatReturn.newVersions, activeEffective);
        assertInactivatedVersions(inactiveCount, inactiveEffective, journal.read().inactiveVersions());
    }

    @ParameterizedTest
    @MethodSource({ "createJournalUpdateCases", "createJournalEdgeCases" })
    public void testFunctionAccept_Bitemporal(ZonedDateTime updateFrom, ZonedDateTime updateUntil, JournalUpdateCase updateCase,
            int countOfNewVersions, List<ZonedDateTime> activeEffective, int inactiveCount,
            List<ZonedDateTime> inactiveEffective) throws Exception {
        context = BTExecutionContext.INSTANCE.barbel(DefaultDocument.class).withMode(BarbelMode.BITEMPORAL)
                .withUser("testUser");
        DocumentJournal journal = DocumentJournal.create(ProcessingState.INTERNAL, context,
                BarbelTestHelper.generateJournalOfDefaultDocuments("someId", Arrays.asList(
                        ZonedDateTime.parse("2016-01-01T00:00:00Z"),
                        ZonedDateTime.parse("2017-01-01T00:00:00Z"), ZonedDateTime.parse("2018-01-01T00:00:00Z"), ZonedDateTime.parse(
                                "2019-01-01T00:00:00Z"))),
                "someId");
        UpdateReturn updatReturn = performUpdate_Bitemporal(updateFrom, updateUntil, journal);
        assertTrue(updatReturn.newVersions.size() == countOfNewVersions);
        assertEquals(updateCase, updatReturn.function.getActualCase());
        assertNewVersions(updatReturn.bitemporal, updatReturn.newVersions, activeEffective);
        assertInactivatedVersions(inactiveCount, inactiveEffective, journal.read().inactiveVersions());
    }

    private void assertInactivatedVersions(int inactiveCount, List<ZonedDateTime> inactiveEffective,
            List<? extends Bitemporal> inactivatedVersions) {
        assertEquals(inactiveCount, inactivatedVersions.size());
        for (int i = 0; i < inactivatedVersions.size(); i++) {
            assertInactivatedVersion(inactivatedVersions.get(i), inactiveEffective.get(i * 2),
                    inactiveEffective.get(i * 2 + 1));
        }
    }

    private UpdateReturn performUpdate_Pojo(ZonedDateTime from, ZonedDateTime until, DocumentJournal journal) {
        DefaultPojo update = new DefaultPojo();
        update.setDocumentId("someId");
        update.setData("some data");
        Bitemporal bitemporal = context.getMode().snapshotMaiden(context, update,
                BitemporalStamp.createActive(context, "someId", EffectivePeriod.of(from, until)));
        EmbeddingJournalUpdateStrategy updateStrategy = new EmbeddingJournalUpdateStrategy(context);
        updateStrategy.accept(journal, bitemporal);
        return new UpdateReturn(journal.getLastInserts(), bitemporal, updateStrategy);
    }

    private UpdateReturn performUpdate_Bitemporal(ZonedDateTime from, ZonedDateTime until, DocumentJournal journal) {
        DefaultDocument doc = new DefaultDocument();
        Bitemporal bitemporal = BarbelMode.BITEMPORAL.snapshotMaiden(context, doc,
                BitemporalStamp.createActive(context, "someId", EffectivePeriod.of(from, until)));

        EmbeddingJournalUpdateStrategy function = new EmbeddingJournalUpdateStrategy(context);
        function.accept(journal, bitemporal);
        List<Bitemporal> list = journal.getLastInserts();
        return new UpdateReturn(list, bitemporal, function);
    }

    private void assertNewVersions(Bitemporal insertedBitemporal, List<? extends Bitemporal> newVersions,
            List<ZonedDateTime> activeEffective) {

        for (int i = 0; i < newVersions.size(); i++) {
            assertEquals(activeEffective.get(i * 2), newVersions.get(i).getBitemporalStamp().getEffectiveTime().from());
            assertEquals(activeEffective.get(i * 2 + 1),
                    newVersions.get(i).getBitemporalStamp().getEffectiveTime().until());
            assertEquals(ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.of("Z")),
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

    private void assertInactivatedVersion(Bitemporal inactivated, ZonedDateTime from, ZonedDateTime until) {

        assertEquals(from, inactivated.getBitemporalStamp().getEffectiveTime().from());
        assertEquals(until, inactivated.getBitemporalStamp().getEffectiveTime().until());

        assertEquals(ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.of("Z")),
                inactivated.getBitemporalStamp().getRecordTime().getCreatedAt());
        assertEquals(ZonedDateTime.of(LocalDateTime.of(2019, 1, 30, 10, 0), ZoneId.of("Z")),
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
