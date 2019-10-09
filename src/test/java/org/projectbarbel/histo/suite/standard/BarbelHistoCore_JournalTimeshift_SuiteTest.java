package org.projectbarbel.histo.suite.standard;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelQueries;
import org.projectbarbel.histo.BarbelQueryOptions;
import org.projectbarbel.histo.DocumentJournal;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalObjectState;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTTestStandard;

import io.github.benas.randombeans.api.EnhancedRandom;

@ExtendWith(BTTestStandard.class)
public class BarbelHistoCore_JournalTimeshift_SuiteTest {

    private BarbelHisto<DefaultPojo> core;

    @BeforeEach
    public void setup() {
        core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDate.of(2019, 2, 6).atStartOfDay().atZone(ZoneId.of("Z")));
    }

    @AfterAll
    public static void tearDown() {
        BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
    }

    @Test
    public void testRetrieve() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        core.save(pojo);
        assertEquals(1,
                core.retrieve(and(BarbelQueries.all(pojo.getDocumentId()), BarbelQueries.all(pojo.getDocumentId())))
                        .size());
    }

    @Test
    public void testSave() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        assertNotNull(core.save(pojo));
    }

    //// @formatter:off
	/**
	 * A |-------------------------------------------------> infinite N
	 * |-----------------------> infinite expecting three versions I
	 * |-------------------------------------------------> infinite A
	 * |------------------------>|-----------------------> infinite
	 */
	// @formatter:on
    @Test
    public void testSave_twoVersions_case_1() {

        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDate.of(2019, 2, 6).atStartOfDay().atZone(ZoneId.of("Z")));

        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        core.save(pojo, BarbelHistoContext.getBarbelClock().now());
        DefaultPojo saveForLater = core.retrieveOne(BarbelQueries.effectiveNow(pojo.getDocumentId()));

        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDate.of(2019, 2, 10).atStartOfDay().atZone(ZoneId.of("Z")));

        pojo.setData("some new data");
        core.save(pojo, BarbelHistoContext.getBarbelClock().now());

        // checking complete archive
        List<DefaultPojo> all = core.retrieve(BarbelQueries.all(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, all.stream().count());

        // checking inactive
        List<DefaultPojo> allInactive = core.retrieve(BarbelQueries.allInactive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(1, allInactive.stream().count());
        assertEquals(BarbelHistoContext.getBarbelClock().now().minusDays(4),
                ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().isInfinite());

        // checking active journal
        List<DefaultPojo> result = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(2, result.stream().count());
        assertEquals(BarbelHistoContext.getBarbelClock().now().minusDays(4),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(BarbelHistoContext.getBarbelClock().now(),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(BarbelHistoContext.getBarbelClock().now(),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().isInfinite());

        // introducing time shift
        System.out.println(core.prettyPrintJournal(pojo.getDocumentId()));
        String backboneJournalPriorTimeShift = core.prettyPrintJournal(pojo.getDocumentId());

        DocumentJournal timeshift = core.timeshift(pojo.getDocumentId(),
                BarbelHistoContext.getBarbelClock().now().minusDays(1));

        assertNotNull(timeshift.read().effectiveNow());
        assertTrue(timeshift.read().activeVersions().size() == 1);
        assertEquals(timeshift.read().effectiveNow().get(), saveForLater);

        System.out.println(core.prettyPrintJournal(pojo.getDocumentId()));
        String backboneJournalAfterTimeShift = core.prettyPrintJournal(pojo.getDocumentId());

        assertEquals(backboneJournalPriorTimeShift, backboneJournalAfterTimeShift);

    }

    //// @formatter:off
	/**
	 * A |-------------------|-----------------------------> N
	 * |-----------------------> infinite expecting I
	 * |-----------------------------> Infinite A
	 * |-------------------|---->|-----------------------> infinite
	 */
	// @formatter:on
    @Test
    public void testSave_twoVersions_case_3() {

        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDate.of(2019, 2, 6).atStartOfDay().atZone(ZoneId.of("Z")));

        // saving two versions
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        core.save(pojo, now.minusDays(100),
                now.minusDays(8));
        pojo.setData("some new data");
        core.save(pojo, now.minusDays(8));

        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDate.of(2019, 2, 10).atStartOfDay().atZone(ZoneId.of("Z")));
        now = BarbelHistoContext.getBarbelClock().now();

        pojo.setData("some more data");
        core.save(pojo, now);

        // checking complete archive
        System.out.println(core.prettyPrintJournal(pojo.getDocumentId()));
        List<DefaultPojo> all = core.retrieve(BarbelQueries.all(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(4, all.stream().count());

        // checking inactive
        List<DefaultPojo> allInactive = core.retrieve(BarbelQueries.allInactive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(1, allInactive.stream().count());
        assertEquals(now.minusDays(12),
                ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().isInfinite());
        assertEquals("some new data", allInactive.get(0).getData());

        // checking active journal
        List<DefaultPojo> result = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, result.stream().count());
        assertEquals(now.minusDays(104),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(now.minusDays(12),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(now.minusDays(12),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(now,
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(now,
                ((Bitemporal) result.get(2)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) result.get(2)).getBitemporalStamp().getEffectiveTime().isInfinite());
        assertEquals("some more data", result.get(2).getData());

        List<DefaultPojo> effectiveNow = core.retrieve(BarbelQueries.effectiveNow(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        DefaultPojo effecive = effectiveNow.stream().findFirst().get();
        assertEquals("some more data", effecive.getData());

        List<DefaultPojo> effectiveYesterday = core.retrieve(
                BarbelQueries.effectiveAt(pojo.getDocumentId(),
                        now.minusDays(1)),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        DefaultPojo effecive2 = effectiveYesterday.stream().findFirst().get();
        assertEquals("some new data", effecive2.getData());

        // shifting time back to original journal
        DocumentJournal shift = core.timeshift(pojo.getDocumentId(),
                now.minusDays(2));

        // should only be two active
        assertEquals(2, shift.read().activeVersions().size());

        result = shift.read().activeVersions();

        // should have old initial effective periods
        assertEquals(now.minusDays(104),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(now.minusDays(12),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(now.minusDays(12),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().isInfinite());

        // timeshift never has inactive versions
        assertEquals(0, shift.read().inactiveVersions().size());

    }

    @Test
    public void testSave_AlwaysCopies() {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        core.save(pojo, BarbelHistoContext.getBarbelClock().now().minusDays(100),
                BarbelHistoContext.getBarbelClock().now().minusDays(8));
        DocumentJournal shift = core.timeshift(pojo.getDocumentId(), ZonedDateTime.parse("2019-02-06T00:00:00Z"));
        DefaultPojo copy = (DefaultPojo) shift.read().activeVersions().get(0);
        assertNotSame(copy, pojo);
        assertNotSame(((BarbelProxy) copy).getTarget(), pojo);
        DefaultPojo another = (DefaultPojo) shift.read().activeVersions().get(0);
        assertNotSame(copy, another);
    }

    //// @formatter:off
	/**
	 * A |-------------------|---->|-----------------------> infinite N
	 * |-------------| expecting three inactivated and three new periods I
	 * |-------------------|-----|-----------------------> A
	 * |---------------|-------------|-------------------> infinite
	 */
	// @formatter:on
    @Test
    public void testSave_twoVersions_case_4() {

        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDate.of(2019, 2, 6).atStartOfDay().atZone(ZoneId.of("Z")));

        // saving two versions
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        String originalData = pojo.getData();
        core.save(pojo, ZonedDateTime.parse("2018-10-01T00:00:00Z"), ZonedDateTime.parse("2019-02-01T00:00:00Z"));
        pojo.setData("2nd Period");
        core.save(pojo, ZonedDateTime.parse("2019-02-01T00:00:00Z"), ZonedDateTime.parse("2019-02-06T00:00:00Z"));
        pojo.setData("3rd Period");
        core.save(pojo, ZonedDateTime.parse("2019-02-06T00:00:00Z"));

        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDate.of(2019, 2, 10).atStartOfDay().atZone(ZoneId.of("Z")));

        pojo.setData("4td interrupting Period");
        core.save(pojo, ZonedDateTime.parse("2019-01-25T00:00:00Z"), ZonedDateTime.parse("2019-02-10T00:00:00Z"));

        // checking complete archive
        System.out.println(core.prettyPrintJournal(pojo.getDocumentId()));
        List<DefaultPojo> all = core.retrieve(BarbelQueries.all(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(6, all.stream().count());

        // checking inactive
        List<DefaultPojo> allInactive = core.retrieve(BarbelQueries.allInactive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, allInactive.stream().count());
        assertEquals(ZonedDateTime.parse("2018-10-01T00:00:00Z"),
                ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(ZonedDateTime.parse("2019-02-01T00:00:00Z"),
                ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(ZonedDateTime.parse("2019-02-01T00:00:00Z"),
                ((Bitemporal) allInactive.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(ZonedDateTime.parse("2019-02-06T00:00:00Z"),
                ((Bitemporal) allInactive.get(1)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(ZonedDateTime.parse("2019-02-06T00:00:00Z"),
                ((Bitemporal) allInactive.get(2)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) allInactive.get(2)).getBitemporalStamp().getEffectiveTime().isInfinite());
        assertEquals(originalData, allInactive.get(0).getData());
        assertEquals("2nd Period", allInactive.get(1).getData());
        assertEquals("3rd Period", allInactive.get(2).getData());

        // checking active journal
        List<DefaultPojo> result = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, result.stream().count());
        assertEquals(ZonedDateTime.parse("2018-10-01T00:00:00Z"),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(ZonedDateTime.parse("2019-01-25T00:00:00Z"),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(ZonedDateTime.parse("2019-01-25T00:00:00Z"),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(ZonedDateTime.parse("2019-02-10T00:00:00Z"),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(ZonedDateTime.parse("2019-02-10T00:00:00Z"),
                ((Bitemporal) result.get(2)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) result.get(2)).getBitemporalStamp().getEffectiveTime().isInfinite());
        assertEquals(originalData, result.get(0).getData());
        assertEquals("4td interrupting Period", result.get(1).getData());
        assertEquals("3rd Period", result.get(2).getData());

        // time shift
        DocumentJournal shift = core.timeshift(pojo.getDocumentId(), LocalDate.of(2019, 2, 8).atStartOfDay().atZone(ZoneId.of("Z")));
        List<DefaultPojo> shiftedJournal = shift.read().activeVersions();

        // the initial journal
        assertEquals(3, shiftedJournal.stream().count());
        assertEquals(ZonedDateTime.parse("2018-10-01T00:00:00Z"),
                ((Bitemporal) shiftedJournal.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(ZonedDateTime.parse("2019-02-01T00:00:00Z"),
                ((Bitemporal) shiftedJournal.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(ZonedDateTime.parse("2019-02-01T00:00:00Z"),
                ((Bitemporal) shiftedJournal.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(ZonedDateTime.parse("2019-02-06T00:00:00Z"),
                ((Bitemporal) shiftedJournal.get(1)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(ZonedDateTime.parse("2019-02-06T00:00:00Z"),
                ((Bitemporal) shiftedJournal.get(2)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) shiftedJournal.get(2)).getBitemporalStamp().getEffectiveTime().isInfinite());

        // all active
        assertEquals(BitemporalObjectState.ACTIVE,
                ((Bitemporal) shiftedJournal.get(2)).getBitemporalStamp().getRecordTime().getState());
        assertEquals(BitemporalObjectState.ACTIVE,
                ((Bitemporal) shiftedJournal.get(1)).getBitemporalStamp().getRecordTime().getState());
        assertEquals(BitemporalObjectState.ACTIVE,
                ((Bitemporal) shiftedJournal.get(0)).getBitemporalStamp().getRecordTime().getState());

    }

}
