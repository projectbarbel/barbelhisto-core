package org.projectbarbel.histo.suite.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.BarbelQueries;
import org.projectbarbel.histo.BarbelTestHelper;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.model.RecordPeriod;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTTestStandard;

import io.github.benas.randombeans.api.EnhancedRandom;
import net.sf.cglib.proxy.Enhancer;

@ExtendWith(BTTestStandard.class)
public class BarbelHistoCore_LoadUnload_SuiteTest {

    @BeforeEach
    public void setUp() {
        BTExecutionContext.INSTANCE.clearResources();
    }

    @Test
    public void testCompleteCycle_Pojo() {
        BarbelHisto<DefaultPojo> histo = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        List<Bitemporal> bitemporals = Arrays.asList(
                new BitemporalVersion(BitemporalStamp.of("test", "some", EffectivePeriod.nowToInfinite(),
                        RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)),
                new BitemporalVersion(BitemporalStamp.of("test", "someOther", EffectivePeriod.nowToInfinite(),
                        RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)));
        int count = bitemporals.size();
        histo.load(bitemporals);
        histo.load(histo.unload("some", "someOther"));
        assertEquals(count, histo.retrieve(BarbelQueries.all()).size());
        assertTrue(Enhancer.isEnhanced(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
        assertTrue(((BitemporalVersion) bitemporals.iterator().next()).getObject().getClass()
                .isAssignableFrom(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
    }

    @Test
    public void testPartialUnload_Pojo() {
        BarbelHisto<DefaultPojo> histo = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        List<Bitemporal> bitemporals = Arrays.asList(
                new BitemporalVersion(BitemporalStamp.of("test", "some", EffectivePeriod.nowToInfinite(),
                        RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)),
                new BitemporalVersion(BitemporalStamp.of("test", "someOther", EffectivePeriod.nowToInfinite(),
                        RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)));
        int count = bitemporals.size();
        histo.load(bitemporals);
        histo.load(histo.unload("some"));
        assertEquals(count, histo.retrieve(BarbelQueries.all()).size());
        assertTrue(Enhancer.isEnhanced(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
        assertTrue(((BitemporalVersion) bitemporals.iterator().next()).getObject().getClass()
                .isAssignableFrom(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
    }

    @Test
    public void testPartialUnloadAndCount_Pojo() {
        BarbelHisto<DefaultPojo> histo = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        List<Bitemporal> bitemporals = Arrays.asList(
                new BitemporalVersion(BitemporalStamp.of("test", "some", EffectivePeriod.nowToInfinite(),
                        RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)),
                new BitemporalVersion(BitemporalStamp.of("test", "someOther", EffectivePeriod.nowToInfinite(),
                        RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)));
        histo.load(bitemporals);
        assertEquals(2, histo.retrieve(BarbelQueries.all()).size());
        histo.unload("some");
        assertEquals(1, histo.retrieve(BarbelQueries.all()).size());
        assertTrue(Enhancer.isEnhanced(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
        assertTrue(((BitemporalVersion) bitemporals.iterator().next()).getObject().getClass()
                .isAssignableFrom(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
    }

    @Test
    public void testPopulateBitemporalVersion_emptyArray() throws Exception {
        BarbelHisto<DefaultPojo> histo = BTExecutionContext.INSTANCE.barbel(BitemporalVersion.class).build();
        assertThrows(IllegalArgumentException.class, () -> histo.unload());
    }

    @Test
    public void testPopulateBitemporalVersion_emptyBackbone() throws Exception {
        BarbelHisto<DefaultPojo> histo = BTExecutionContext.INSTANCE.barbel(BitemporalVersion.class).build();
        assertThrows(IllegalStateException.class, () -> histo.unload("some"));
    }

    @Test
    public void testPopulateBitemporalVersion_DocIDExists() throws Exception {
        BarbelHisto<DefaultPojo> histo = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        List<Bitemporal> bitemporals = Arrays.asList(
                new BitemporalVersion(BitemporalStamp.of("test", "some", EffectivePeriod.nowToInfinite(),
                        RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)),
                new BitemporalVersion(BitemporalStamp.of("test", "someOther", EffectivePeriod.nowToInfinite(),
                        RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)));
        histo.load(bitemporals);
        assertThrows(IllegalStateException.class, () -> histo.load(bitemporals));
    }

    @Test
    public void testCompleteCycle_Bitemporal() {
        BarbelHisto<DefaultDocument> histo = BTExecutionContext.INSTANCE.barbel(DefaultDocument.class)
                .withMode(BarbelMode.BITEMPORAL).build();
        List<Bitemporal> bitemporals = Arrays.asList(new DefaultDocument("some", BitemporalStamp.createActive("some"), "some data"),
                new DefaultDocument("someOther", BitemporalStamp.createActive("someOther"), "some data"));
        int count = bitemporals.size();
        histo.load(bitemporals);
        Collection<Bitemporal> docs = histo.unload("some", "someOther");
        histo.load(docs);
        assertEquals(count, histo.retrieve(BarbelQueries.all()).size());
        assertTrue(!Enhancer.isEnhanced(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
        assertTrue(((DefaultDocument) bitemporals.iterator().next()).getClass()
                .isAssignableFrom(histo.retrieve(BarbelQueries.all()).stream().findFirst().get().getClass()));
    }

    @Test
    public void testCompleteCycle_Bitemporal_IDs_Differ() {
        BarbelHisto<DefaultDocument> histo = BTExecutionContext.INSTANCE.barbel(DefaultDocument.class)
                .withMode(BarbelMode.BITEMPORAL).build();
        List<Bitemporal> bitemporals = Arrays.asList(BarbelTestHelper.random(DefaultDocument.class),
                BarbelTestHelper.random(DefaultDocument.class));
        ((DefaultDocument) bitemporals.get(0)).setId("some");
        ((DefaultDocument) bitemporals.get(1)).setId("someOther");
        assertThrows(IllegalArgumentException.class, () -> histo.load(bitemporals));
    }
    
}
