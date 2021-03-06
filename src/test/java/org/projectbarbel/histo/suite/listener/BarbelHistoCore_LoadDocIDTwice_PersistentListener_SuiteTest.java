package org.projectbarbel.histo.suite.listener;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.event.HistoEventFailedException;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.model.RecordPeriod;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTPersistenceListenerOnly;

import io.github.benas.randombeans.api.EnhancedRandom;

@BTPersistenceListenerOnly
public class BarbelHistoCore_LoadDocIDTwice_PersistentListener_SuiteTest {
    @Test
    public void testPopulateBitemporalVersion_DocIDExistsPersistent() throws Exception {
        BarbelHisto<DefaultPojo> histo1 = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        List<Bitemporal> bitemporals = Arrays.asList(
                new BitemporalVersion(BitemporalStamp.of("test", "some", EffectivePeriod.nowToInfinite(),
                        RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)),
                new BitemporalVersion(BitemporalStamp.of("test", "someOther", EffectivePeriod.nowToInfinite(),
                        RecordPeriod.createActive()), EnhancedRandom.random(DefaultPojo.class)));
        histo1.load(bitemporals);
        BarbelHisto<DefaultPojo> histo2 = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        assertThrows(HistoEventFailedException.class, () -> histo2.load(bitemporals));
    }

}
