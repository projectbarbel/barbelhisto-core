package org.projectbarbel.histo.suite.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTTestPersitenceListenerOnly;
import org.projectbarbel.histo.suite.persistent.BarbelHistoCore_MultiUpdate_andQuery;

@ExtendWith(BTTestPersitenceListenerOnly.class)
public class BarbelHistoCore_MultiUpdate_andQuery_Listener_SuiteTest extends BarbelHistoCore_MultiUpdate_andQuery {

    @Order(11)
    @Test
    public void addSomeMoreData() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        DefaultPojo pojo = new DefaultPojo("someOther", "some data");
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        assertEquals(1, ((BarbelHistoCore<DefaultPojo>)core).size());
    }

}
