package org.projectbarbel.histo.suite.persistent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTCQPersistenceOnly;

@BTCQPersistenceOnly
public class BarbelHistoCore_MultiUpdate_andQueryBitemporal_SuiteTest
        extends BarbelHistoCore_MultiUpdate_andQueryBitemporal {

    @Order(11)
    @Test
    public void addSomeMoreData() throws Exception {
        BarbelHisto<DefaultDocument> core = BTExecutionContext.INSTANCE.barbel(DefaultDocument.class)
                .withMode(BarbelMode.BITEMPORAL).build();
        DefaultDocument pojo = new DefaultDocument("someOther", "some data");
        core.save(pojo);
        assertEquals(25, ((BarbelHistoCore<DefaultDocument>) core).size());
    }
}
