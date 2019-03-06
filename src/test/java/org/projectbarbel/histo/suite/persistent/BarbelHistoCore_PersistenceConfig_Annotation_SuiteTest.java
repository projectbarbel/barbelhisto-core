package org.projectbarbel.histo.suite.persistent;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.pojos.PojoWOPersistenceConfig;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTCQPersistenceOnly;

import io.github.benas.randombeans.api.EnhancedRandom;

@BTCQPersistenceOnly
public class BarbelHistoCore_PersistenceConfig_Annotation_SuiteTest {

    @Test
    public void testSavePojoInBitemporal() throws Exception {
        BarbelHisto<PojoWOPersistenceConfig> core = BTExecutionContext.INSTANCE.barbel(PojoWOPersistenceConfig.class).build();
        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> core.save(EnhancedRandom.random(PojoWOPersistenceConfig.class), LocalDate.now(), LocalDate.MAX));
        assertTrue(exc.getMessage().contains("@PersistenceConfig"));
    }

}
