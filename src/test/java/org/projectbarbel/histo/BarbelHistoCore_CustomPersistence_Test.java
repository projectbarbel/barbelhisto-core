package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHistoCore.DumpMode;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;
import org.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;
import net.sf.cglib.proxy.Enhancer;

public class BarbelHistoCore_CustomPersistence_Test {

    @Test
    public void testPopulateBitemporalVersion() {
        BarbelHisto<DefaultPojo> histo = BarbelHistoBuilder.barbel().build();
        List<Bitemporal> bitemporals = Arrays.asList(
                new BitemporalVersion<>(BitemporalStamp.createActive(), EnhancedRandom.random(DefaultPojo.class)),
                new BitemporalVersion<>(BitemporalStamp.createActive(), EnhancedRandom.random(DefaultPojo.class)));
        int count = bitemporals.size();
        histo.populate(bitemporals);
        histo.populate(histo.dump(DumpMode.CLEARCOLLECTION));
        assertEquals(count, histo.retrieve(BarbelQueries.all()).size());
        assertTrue(Enhancer.isEnhanced(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
        assertTrue(((BitemporalVersion<?>) bitemporals.iterator().next()).getObject().getClass()
                .isAssignableFrom(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
    }

    @Test
    public void testPopulateBitemporal() {
        BarbelHisto<BitemporalVersion<?>> histo = BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL.get()).build();
        List<Bitemporal> bitemporals = Arrays.asList(
                new BitemporalVersion<>(BitemporalStamp.createActive(), EnhancedRandom.random(DefaultPojo.class)),
                new BitemporalVersion<>(BitemporalStamp.createActive(), EnhancedRandom.random(DefaultPojo.class)));
        int count = bitemporals.size();
        histo.populate(bitemporals);
        histo.populate(histo.dump(DumpMode.CLEARCOLLECTION));
        assertEquals(count, histo.retrieve(BarbelQueries.all()).size());
        assertTrue(!Enhancer.isEnhanced(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
        assertTrue(((BitemporalVersion<?>) bitemporals.iterator().next()).getClass()
                .isAssignableFrom(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
    }
    
}
