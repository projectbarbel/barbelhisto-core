package org.projectbarbel.histo.suite.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.BarbelQueries;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTC_Standard;

@ExtendWith(BTC_Standard.class)
public class BarbelHistoCore_MultiUpdate_MAX_TestSuite {

    @Test
    void embeddedOverlap_Max() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        DefaultPojo pojo = new DefaultPojo("somePojo", "some data");
        
        // Now |---------------------------------| MAX
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        assertEquals(1, core.retrieve(BarbelQueries.allActive("somePojo")).size());
        assertEquals(0, core.retrieve(BarbelQueries.allInactive("somePojo")).size());
        
        // Now |---------------------------------| MAX
        //      |--------------------------------| Max
        //     ||--------------------------------| Max
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.MAX);
        assertEquals(3,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("somePojo")).size());
        assertEquals(1, core.retrieve(BarbelQueries.allInactive("somePojo")).size());
        
        //     ||--------------------------------| Max
        //        5|-----------------------------| MAX
        //     ||--|-----------------------------| Max
        core.save(pojo, LocalDate.now().plusDays(5), LocalDate.MAX);
        assertEquals(5,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("somePojo")).size());
        assertEquals(2, core.retrieve(BarbelQueries.allInactive("somePojo")).size());
        
    }

}
