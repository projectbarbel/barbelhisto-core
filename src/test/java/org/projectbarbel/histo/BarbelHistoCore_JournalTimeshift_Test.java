package org.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCore_JournalTimeshift_Test {

    private BarbelHisto<DefaultPojo> core;

    @BeforeEach
    public void setup() {
        core = BarbelHistoBuilder.barbel().build();
        BarbelHistoContext.getDefaultClock().useFixedClockAt(LocalDate.of(2019, 2, 6).atStartOfDay());
    }

    @Test
    public void testRetrieve() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        assertEquals(1,
                core.retrieve(and(BarbelQueries.all(pojo.getDocumentId()), BarbelQueries.all(pojo.getDocumentId())))
                        .size());
    }

    @Test
    public void testSave() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        assertTrue(core.save(pojo, LocalDate.now(), LocalDate.MAX));
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

        BarbelHistoContext.getDefaultClock().useFixedClockAt(LocalDate.of(2019, 2, 6).atStartOfDay());
        
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        core.save(pojo, BarbelHistoContext.getDefaultClock().today(), LocalDate.MAX);
        Optional<DefaultPojo> saveForLater = core.retrieveOne(BarbelQueries.effectiveNow(pojo.getDocumentId()));

        BarbelHistoContext.getDefaultClock().useFixedClockAt(LocalDate.of(2019, 2, 10).atStartOfDay());
        
        pojo.setData("some new data");
        core.save(pojo, BarbelHistoContext.getDefaultClock().today(), LocalDate.MAX);

        // checking complete archive
        List<DefaultPojo> all = core.retrieve(BarbelQueries.all(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, all.stream().count());

        // checking inactive
        List<DefaultPojo> allInactive = core.retrieve(BarbelQueries.allInactive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(1, allInactive.stream().count());
        assertEquals(BarbelHistoContext.getDefaultClock().today().minusDays(4),
                ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(LocalDate.MAX, ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().until());

        // checking active journal
        List<DefaultPojo> result = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(2, result.stream().count());
        assertEquals(BarbelHistoContext.getDefaultClock().today().minusDays(4),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(BarbelHistoContext.getDefaultClock().today(),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(BarbelHistoContext.getDefaultClock().today(),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(LocalDate.MAX, ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().until());

        // introducing time shift
        System.out.println(core.prettyPrintJournal(pojo.getDocumentId()));
        String backboneJournalPriorTimeShift = core.prettyPrintJournal(pojo.getDocumentId());
        
        DocumentJournal timeshift = core.timeshift(pojo.getDocumentId(), BarbelHistoContext.getDefaultClock().today().minusDays(1).atStartOfDay()) ;

        assertNotNull(timeshift.read().effectiveNow());
        assertTrue(timeshift.read().activeVersions().size()==1);
        assertEquals(timeshift.read().effectiveNow().get(), saveForLater.get());
        
        System.out.println(core.prettyPrintJournal(pojo.getDocumentId()));
        String backboneJournalAfterTimeShift = core.prettyPrintJournal(pojo.getDocumentId());

        assertEquals(backboneJournalPriorTimeShift, backboneJournalAfterTimeShift);

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
        core.save(pojo, BarbelHistoContext.getDefaultClock().today().minusDays(100),
                BarbelHistoContext.getDefaultClock().today().minusDays(8));
        pojo.setData("some new data");
        core.save(pojo, BarbelHistoContext.getDefaultClock().today().minusDays(8), LocalDate.MAX);
        pojo.setData("some more data");
        core.save(pojo, BarbelHistoContext.getDefaultClock().today(), LocalDate.MAX);

        // checking complete archive
        System.out.println(core.prettyPrintJournal(pojo.getDocumentId()));
        List<DefaultPojo> all = core.retrieve(BarbelQueries.all(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(4, all.stream().count());

        // checking inactive
        List<DefaultPojo> allInactive = core.retrieve(BarbelQueries.allInactive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(1, allInactive.stream().count());
        assertEquals(BarbelHistoContext.getDefaultClock().today().minusDays(8),
                ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(LocalDate.MAX, ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals("some new data", allInactive.get(0).getData());

        // checking active journal
        List<DefaultPojo> result = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, result.stream().count());
        assertEquals(BarbelHistoContext.getDefaultClock().today().minusDays(100),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(BarbelHistoContext.getDefaultClock().today().minusDays(8),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(BarbelHistoContext.getDefaultClock().today().minusDays(8),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(BarbelHistoContext.getDefaultClock().today(),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(BarbelHistoContext.getDefaultClock().today(),
                ((Bitemporal) result.get(2)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(LocalDate.MAX, ((Bitemporal) result.get(2)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals("some more data", result.get(2).getData());

        List<DefaultPojo> effectiveNow = core.retrieve(BarbelQueries.effectiveNow(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        DefaultPojo effecive = effectiveNow.stream().findFirst().get();
        assertEquals("some more data", effecive.getData());

        List<DefaultPojo> effectiveYesterday = core.retrieve(BarbelQueries.effectiveAt(pojo.getDocumentId(), BarbelHistoContext.getDefaultClock().today().minusDays(1)),
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
        core.save(pojo, BarbelHistoContext.getDefaultClock().today().minusDays(100),
                BarbelHistoContext.getDefaultClock().today().minusDays(8));
        pojo.setData("2nd Period");
        core.save(pojo, BarbelHistoContext.getDefaultClock().today().minusDays(8), BarbelHistoContext.getDefaultClock().today());
        pojo.setData("3rd Period");
        core.save(pojo, BarbelHistoContext.getDefaultClock().today(), LocalDate.MAX);
        pojo.setData("4td interrupting Period");
        core.save(pojo, BarbelHistoContext.getDefaultClock().today().minusDays(10), BarbelHistoContext.getDefaultClock().today().plusDays(10));

        // checking complete archive
        System.out.println(core.prettyPrintJournal(pojo.getDocumentId()));
        List<DefaultPojo> all = core.retrieve(BarbelQueries.all(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(6, all.stream().count());

        // checking inactive
        List<DefaultPojo> allInactive = core.retrieve(BarbelQueries.allInactive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, allInactive.stream().count());
        assertEquals(BarbelHistoContext.getDefaultClock().today().minusDays(100),
                ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(BarbelHistoContext.getDefaultClock().today().minusDays(8), ((Bitemporal) allInactive.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(BarbelHistoContext.getDefaultClock().today().minusDays(8),
                ((Bitemporal) allInactive.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(BarbelHistoContext.getDefaultClock().today(), ((Bitemporal) allInactive.get(1)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(BarbelHistoContext.getDefaultClock().today(),
                ((Bitemporal) allInactive.get(2)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(LocalDate.MAX, ((Bitemporal) allInactive.get(2)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(originalData, allInactive.get(0).getData());
        assertEquals("2nd Period", allInactive.get(1).getData());
        assertEquals("3rd Period", allInactive.get(2).getData());

        // checking active journal
        List<DefaultPojo> result = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(3, result.stream().count());
        assertEquals(BarbelHistoContext.getDefaultClock().today().minusDays(100),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(BarbelHistoContext.getDefaultClock().today().minusDays(10),
                ((Bitemporal) result.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(BarbelHistoContext.getDefaultClock().today().minusDays(10),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(BarbelHistoContext.getDefaultClock().today().plusDays(10),
                ((Bitemporal) result.get(1)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(BarbelHistoContext.getDefaultClock().today().plusDays(10),
                ((Bitemporal) result.get(2)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(LocalDate.MAX, ((Bitemporal) result.get(2)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(originalData, result.get(0).getData());
        assertEquals("4td interrupting Period", result.get(1).getData());
        assertEquals("3rd Period", result.get(2).getData());

        List<DefaultPojo> effectiveNow = core.retrieve(BarbelQueries.effectiveNow(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        DefaultPojo effectiveNowPojo = effectiveNow.stream().findFirst().get();
        assertEquals("4td interrupting Period", effectiveNowPojo.getData());

        List<DefaultPojo> effective11DaysAgo = core.retrieve(BarbelQueries.effectiveAt(pojo.getDocumentId(), BarbelHistoContext.getDefaultClock().today().minusDays(11)),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        DefaultPojo effective11DaysAgoPojo = effective11DaysAgo.stream().findFirst().get();
        assertEquals(originalData, effective11DaysAgoPojo.getData());
        
        List<DefaultPojo> effectiveIn11Days = core.retrieve(BarbelQueries.effectiveAt(pojo.getDocumentId(), BarbelHistoContext.getDefaultClock().today().plusDays(11)),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        DefaultPojo effectiveIn11DaysPojo = effectiveIn11Days.stream().findFirst().get();
        assertEquals("3rd Period", effectiveIn11DaysPojo.getData());
        
    }

}
