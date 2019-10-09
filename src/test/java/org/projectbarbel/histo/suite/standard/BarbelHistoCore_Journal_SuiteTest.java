package org.projectbarbel.histo.suite.standard;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTTestStandard;

import io.github.benas.randombeans.api.EnhancedRandom;

@ExtendWith(BTTestStandard.class)
public class BarbelHistoCore_Journal_SuiteTest {

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
     * A |-------------------------------------------------> infinite
     * N                           |-----------------------> infinite
     * expecting three versions
     * I |-------------------------------------------------> infinite
     * A |------------------------>|-----------------------> infinite
     */
    // @formatter:on
    @Test
    public void testSave_twoVersions_case_1() {

        System.out.println("trace");
        // saving two versions
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        core.save(pojo, now);
        pojo.setData("some new data");
        core.save(pojo, now.plusDays(10));

        System.out.println("trace");
        // checking complete archive
        List<DefaultPojo> all = core.retrieve(BarbelQueries.all(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, all.stream().count());

        System.out.println("trace");
        // checking inactive
        List<DefaultPojo> allInactive = core.retrieve(BarbelQueries.allInactive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(1, allInactive.stream().count());
        assertEquals(now,
                ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().isInfinite());

        // checking active journal
        List<DefaultPojo> result = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(2, result.stream().count());
        assertEquals(now,
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(now.plusDays(10),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(now.plusDays(10),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().isInfinite());

    }

    //// @formatter:off
    /**
     * A |-------------------------|-----------------------> infinite
     * N                           |-----------------------> infinite
     * expecting three versions
     * I                           |-----------------------> infinite
     * A |------------------------>|-----------------------> infinite
     */
    // @formatter:on
    @Test
    public void testSave_twoVersions_case_1b() {

        // saving two versions
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        core.save(pojo, now.minusDays(100),
                now.minusDays(8));
        pojo.setData("some new data");
        core.save(pojo, now.minusDays(8));
        pojo.setData("some more data");
        core.save(pojo, now.minusDays(8));

        // checking complete archive
        System.out.println(core.prettyPrintJournal(pojo.getDocumentId()));
        List<DefaultPojo> all = core.retrieve(BarbelQueries.all(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, all.stream().count());

        // checking inactive
        List<DefaultPojo> allInactive = core.retrieve(BarbelQueries.allInactive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(1, allInactive.stream().count());
        assertEquals(now.minusDays(8),
                ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().isInfinite());
        assertEquals("some new data", allInactive.get(0).getData());

        // checking active journal
        List<DefaultPojo> result = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(2, result.stream().count());
        assertEquals(now.minusDays(100),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(now.minusDays(8),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(now.minusDays(8),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().isInfinite());
        assertEquals("some more data", result.get(1).getData());

        List<DefaultPojo> effectiveNow = core.retrieve(BarbelQueries.effectiveNow(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        DefaultPojo effecive = effectiveNow.stream().findFirst().get();
        assertEquals("some more data", effecive.getData());

    }

    //// @formatter:off
    /**
     * A |-------------------|
     * N                           |-----------------------> infinite
     * expecting two versions
     * A |-------------------|     |-----------------------> infinite
     */
    // @formatter:on
    @Test
    public void testSave_twoVersions_case_2() {

        // saving two versions
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        core.save(pojo, now.minusDays(100),
                now.minusDays(10));
        pojo.setData("some new data");
        core.save(pojo, now);

        // checking complete archive
        List<DefaultPojo> all = core.retrieve(BarbelQueries.all(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(2, all.stream().count());

        // checking inactive
        List<DefaultPojo> allInactive = core.retrieve(BarbelQueries.allInactive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(0, allInactive.stream().count());

        // checking active journal
        List<DefaultPojo> allActive = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(2, allActive.stream().count());
        assertEquals(now.minusDays(100),
                ((Bitemporal) allActive.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(now.minusDays(10),
                ((Bitemporal) allActive.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(now,
                ((Bitemporal) allActive.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) allActive.get(1)).getBitemporalStamp().getEffectiveTime().isInfinite());

        // new record today effective
        List<DefaultPojo> effectiveNow = core.retrieve(BarbelQueries.effectiveNow(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        DefaultPojo effecive = effectiveNow.stream().findFirst().get();
        assertEquals("some new data", effecive.getData());

        // yesterday non effective
        List<DefaultPojo> effectiveYesterday = core.retrieve(
                BarbelQueries.effectiveAt(pojo.getDocumentId(), now.minusDays(1)),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(0, effectiveYesterday.size());

        // 14 days ago old effective
        List<DefaultPojo> effective14DaysAgo = core.retrieve(
                BarbelQueries.effectiveAt(pojo.getDocumentId(), now.minusDays(14)),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(1, effective14DaysAgo.size());
        
    }

    //// @formatter:off
    /**
     * A |-------------------|----------------------------->
     * N                           |-----------------------> infinite
     * expecting
     * I                     |-----------------------------> Infinite
     * A |-------------------|---->|-----------------------> infinite
     */
    // @formatter:on
    @Test
    public void testSave_twoVersions_case_3() {
        
        // saving two versions
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        core.save(pojo, now.minusDays(100),
                now.minusDays(8));
        pojo.setData("some new data");
        core.save(pojo, now.minusDays(8));
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
        assertEquals(now.minusDays(8),
                ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().isInfinite());
        assertEquals("some new data", allInactive.get(0).getData());

        // checking active journal
        List<DefaultPojo> result = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, result.stream().count());
        assertEquals(now.minusDays(100),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(now.minusDays(8),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(now.minusDays(8),
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

        List<DefaultPojo> effectiveYesterday = core.retrieve(BarbelQueries.effectiveAt(pojo.getDocumentId(), now.minusDays(1)),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        DefaultPojo effecive2 = effectiveYesterday.stream().findFirst().get();
        assertEquals("some new data", effecive2.getData());
        
    }
    //// @formatter:off
    /**
     * A |-------------------|---->|-----------------------> infinite
     * N                 |-------------| 
     * expecting three inactivated and three new periods
     * I |-------------------|-----|----------------------->
     * A |---------------|-------------|-------------------> infinite
     */
    // @formatter:on
    @Test
    public void testSave_twoVersions_case_4() {
        
        // saving two versions
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        String originalData = pojo.getData();
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        core.save(pojo, now.minusDays(100),
                now.minusDays(8));
        pojo.setData("2nd Period");
        core.save(pojo, now.minusDays(8),
                now);
        pojo.setData("3rd Period");
        core.save(pojo, now);
        pojo.setData("4td interrupting Period");
        core.save(pojo, now.minusDays(10), now.plusDays(10));

        // checking complete archive
        System.out.println(core.prettyPrintJournal(pojo.getDocumentId()));
        List<DefaultPojo> all = core.retrieve(BarbelQueries.all(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(6, all.stream().count());

        // checking inactive
        List<DefaultPojo> allInactive = core.retrieve(BarbelQueries.allInactive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, allInactive.stream().count());
        assertEquals(now.minusDays(100),
                ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(now.minusDays(8), ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(now.minusDays(8),
                ((Bitemporal) allInactive.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(now, ((Bitemporal) allInactive.get(1)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(now,
                ((Bitemporal) allInactive.get(2)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) allInactive.get(2)).getBitemporalStamp().getEffectiveTime().isInfinite());
        assertEquals(originalData, allInactive.get(0).getData());
        assertEquals("2nd Period", allInactive.get(1).getData());
        assertEquals("3rd Period", allInactive.get(2).getData());

        // checking active journal
        List<DefaultPojo> result = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, result.stream().count());
        assertEquals(now.minusDays(100),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(now.minusDays(10),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(now.minusDays(10),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(now.plusDays(10),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(now.plusDays(10),
                ((Bitemporal) result.get(2)).getBitemporalStamp().getEffectiveTime().from());
        assertTrue(((Bitemporal) result.get(2)).getBitemporalStamp().getEffectiveTime().isInfinite());
        assertEquals(originalData, result.get(0).getData());
        assertEquals("4td interrupting Period", result.get(1).getData());
        assertEquals("3rd Period", result.get(2).getData());

        List<DefaultPojo> effectiveNow = core.retrieve(BarbelQueries.effectiveNow(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        DefaultPojo effectiveNowPojo = effectiveNow.stream().findFirst().get();
        assertEquals("4td interrupting Period", effectiveNowPojo.getData());

        List<DefaultPojo> effective11DaysAgo = core.retrieve(BarbelQueries.effectiveAt(pojo.getDocumentId(), now.minusDays(11)),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        DefaultPojo effective11DaysAgoPojo = effective11DaysAgo.stream().findFirst().get();
        assertEquals(originalData, effective11DaysAgoPojo.getData());
        
        List<DefaultPojo> effectiveIn11Days = core.retrieve(BarbelQueries.effectiveAt(pojo.getDocumentId(), now.plusDays(11)),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        DefaultPojo effectiveIn11DaysPojo = effectiveIn11Days.stream().findFirst().get();
        assertEquals("3rd Period", effectiveIn11DaysPojo.getData());
        
    }

    //// @formatter:off
    /**
     * A |-------------------|     |-----------------------> infinite
     * N                 |-------------| 
     * expecting
     * A |---------------|-------------|-------------------> infinite
     */
    // @formatter:on

    //// @formatter:off
    /**
     * A |-------------------|     |-----------------------> infinite
     * N                     |-----| 
     * expecting
     * A |-------------------|-----|-------------------> infinite
     */
    // @formatter:on

}
