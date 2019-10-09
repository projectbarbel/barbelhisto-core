package org.projectbarbel.histo.suite.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.BarbelQueries;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTTestStandard;

@ExtendWith(BTTestStandard.class)
public class BarbelHistoCore_MultiUpdate_MAX_TestSuite {

    @Test
    void embeddedOverlap_Max() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        DefaultPojo pojo = new DefaultPojo("somePojo", "some data");
	    ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
	    // Now |---------------------------------| INFINITE
        core.save(pojo, now, EffectivePeriod.INFINITE);
        assertEquals(1, core.retrieve(BarbelQueries.allActive("somePojo")).size());
        assertEquals(0, core.retrieve(BarbelQueries.allInactive("somePojo")).size());

        // Now |---------------------------------| INFINITE
        //      |--------------------------------| INFINITE
        //     ||--------------------------------| INFINITE
        core.save(pojo, now.plusDays(1), EffectivePeriod.INFINITE);
        assertEquals(3,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("somePojo")).size());
        assertEquals(1, core.retrieve(BarbelQueries.allInactive("somePojo")).size());

        //     ||--------------------------------| INFINITE
        //        5|-----------------------------| INFINITE
        //     ||--|-----------------------------| INFINITE
        core.save(pojo, now.plusDays(5), EffectivePeriod.INFINITE);
        assertEquals(5,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("somePojo")).size());
        assertEquals(2, core.retrieve(BarbelQueries.allInactive("somePojo")).size());

    }

}
