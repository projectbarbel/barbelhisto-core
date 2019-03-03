package org.projectbarbel.histo.suite.persistent;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.model.RecordPeriod;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTC_CQPersistence;

import io.github.benas.randombeans.api.EnhancedRandom;

@ExtendWith(BTC_CQPersistence.class)
public class BarbelHistoCore_LoadDocIDTwice_Persistent_SuiteTest {

    @Test
    public void testPopulateBitemporalVersion_DocIDExistsPersistent() throws Exception {
        BarbelHisto<DefaultPojo> histo1 = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        List<Bitemporal> bitemporals = Arrays.asList(
                new BitemporalVersion(BitemporalStamp.of("test", "some", EffectivePeriod.nowToInfinite(), RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)),
                new BitemporalVersion(BitemporalStamp.of("test", "someOther", EffectivePeriod.nowToInfinite(), RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)));
        histo1.load(bitemporals);
        BarbelHisto<DefaultPojo> histo2 = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        assertThrows(IllegalStateException.class, ()->histo2.load(bitemporals));
    }    

}
