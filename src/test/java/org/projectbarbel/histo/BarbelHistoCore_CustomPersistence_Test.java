package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.model.RecordPeriod;

import io.github.benas.randombeans.api.EnhancedRandom;
import net.sf.cglib.proxy.Enhancer;

public class BarbelHistoCore_CustomPersistence_Test {

    @Test
    public void testCompleteCycle_Pojo() {
        BarbelHisto<DefaultPojo> histo = BarbelHistoBuilder.barbel().build();
        List<Bitemporal> bitemporals = Arrays.asList(
                new BitemporalVersion<>(BitemporalStamp.of("test", "some", EffectivePeriod.nowToInfinite(), RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)),
                new BitemporalVersion<>(BitemporalStamp.of("test", "someOther", EffectivePeriod.nowToInfinite(), RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)));
        int count = bitemporals.size();
        histo.load(bitemporals);
        histo.load(histo.unload("some", "someOther"));
        assertEquals(count, histo.retrieve(BarbelQueries.all()).size());
        assertTrue(Enhancer.isEnhanced(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
        assertTrue(((BitemporalVersion<?>) bitemporals.iterator().next()).getObject().getClass()
                .isAssignableFrom(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
    }

    @Test
    public void testPartialUnload_Pojo() {
    	BarbelHisto<DefaultPojo> histo = BarbelHistoBuilder.barbel().build();
    	List<Bitemporal> bitemporals = Arrays.asList(
    			new BitemporalVersion<>(BitemporalStamp.of("test", "some", EffectivePeriod.nowToInfinite(), RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)),
    			new BitemporalVersion<>(BitemporalStamp.of("test", "someOther", EffectivePeriod.nowToInfinite(), RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)));
    	int count = bitemporals.size();
    	histo.load(bitemporals);
    	histo.load(histo.unload("some"));
    	assertEquals(count, histo.retrieve(BarbelQueries.all()).size());
    	assertTrue(Enhancer.isEnhanced(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
    	assertTrue(((BitemporalVersion<?>) bitemporals.iterator().next()).getObject().getClass()
    			.isAssignableFrom(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
    }
    
    @Test
    public void testPartialUnloadAndCount_Pojo() {
    	BarbelHisto<DefaultPojo> histo = BarbelHistoBuilder.barbel().build();
    	List<Bitemporal> bitemporals = Arrays.asList(
    			new BitemporalVersion<>(BitemporalStamp.of("test", "some", EffectivePeriod.nowToInfinite(), RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)),
    			new BitemporalVersion<>(BitemporalStamp.of("test", "someOther", EffectivePeriod.nowToInfinite(), RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)));
    	histo.load(bitemporals);
    	assertEquals(2, histo.retrieve(BarbelQueries.all()).size());
    	histo.unload("some");
    	assertEquals(1, histo.retrieve(BarbelQueries.all()).size());
    	assertTrue(Enhancer.isEnhanced(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
    	assertTrue(((BitemporalVersion<?>) bitemporals.iterator().next()).getObject().getClass()
    			.isAssignableFrom(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
    }
    
    @Test
	public void testPopulateBitemporalVersion_emptyArray() throws Exception {
        BarbelHisto<DefaultPojo> histo = BarbelHistoBuilder.barbel().build();
        assertThrows(IllegalArgumentException.class, ()->histo.unload());
	}

	@Test
	public void testPopulateBitemporalVersion_emptyBackbone() throws Exception {
        BarbelHisto<DefaultPojo> histo = BarbelHistoBuilder.barbel().build();
        assertThrows(IllegalStateException.class, ()->histo.unload("some"));
	}

	@Test
	public void testPopulateBitemporalVersion_DocIDExists() throws Exception {
        BarbelHisto<DefaultPojo> histo = BarbelHistoBuilder.barbel().build();
        List<Bitemporal> bitemporals = Arrays.asList(
                new BitemporalVersion<>(BitemporalStamp.of("test", "some", EffectivePeriod.nowToInfinite(), RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)),
                new BitemporalVersion<>(BitemporalStamp.of("test", "someOther", EffectivePeriod.nowToInfinite(), RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)));
        histo.load(bitemporals);
        assertThrows(IllegalStateException.class, ()->histo.load(bitemporals));
	}

	@Test
    public void testCompleteCycle_Bitemporal() {
        BarbelHisto<BitemporalVersion<?>> histo = BarbelHistoBuilder.barbel().withMode(BarbelMode.BITEMPORAL).build();
        List<Bitemporal> bitemporals = Arrays.asList(
                new BitemporalVersion<>(BitemporalStamp.of("test", "some", EffectivePeriod.nowToInfinite(), RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)),
                new BitemporalVersion<>(BitemporalStamp.of("test", "someOther", EffectivePeriod.nowToInfinite(), RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)));
        int count = bitemporals.size();
        histo.load(bitemporals);
        histo.load(histo.unload("some", "someOther"));
        assertEquals(count, histo.retrieve(BarbelQueries.all()).size());
        assertTrue(!Enhancer.isEnhanced(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
        assertTrue(((BitemporalVersion<?>) bitemporals.iterator().next()).getClass()
                .isAssignableFrom(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
    }
    
}
