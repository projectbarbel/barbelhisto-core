package com.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCoreTest {

    private BarbelHistoCore<DefaultPojo> core;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Before
    public void setup() {
        core = (BarbelHistoCore) BarbelHistoBuilder.barbel().build();
        BarbelHistoContext.getClock().useFixedClockAt(LocalDate.of(2019, 2, 6).atStartOfDay());
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

    @Test
    public void testSave_twoVersions() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        core.save(pojo, BarbelHistoContext.getClock().today(), LocalDate.MAX);
        pojo.setData("some new data");
        core.save(pojo, BarbelHistoContext.getClock().today().plusDays(10), LocalDate.MAX);
        List<DefaultPojo> result = core.retrieve(BarbelQueries.allActive(pojo.getDocumentId()),
                BarbelQueryOptions.sortAscendingByEffectiveFrom());
        assertEquals(2, result.stream().count());
        assertEquals(BarbelHistoContext.getClock().today(), ((Bitemporal)result.get(0)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(BarbelHistoContext.getClock().today().plusDays(10), ((Bitemporal)result.get(0)).getBitemporalStamp().getEffectiveTime().until());
        assertEquals(BarbelHistoContext.getClock().today().plusDays(10), ((Bitemporal)result.get(1)).getBitemporalStamp().getEffectiveTime().from());
        assertEquals(LocalDate.MAX, ((Bitemporal)result.get(1)).getBitemporalStamp().getEffectiveTime().until());
    }

}
