package org.projectbarbel.histo.suite.persistent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTCQPersistenceOnly;

@BTCQPersistenceOnly
public class BarbelHistoCore_MultiUpdate_andQuery_Nanos_Sub_SuiteTest extends BarbelHistoCore_MultiUpdate_andQuery_Nanos_Sub{

    @BeforeAll
    public static void setUp() {
        BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.of(2019, Month.JANUARY, 30, 8, 0, 0, 0).atZone(ZoneId.systemDefault()));
        now = BarbelHistoContext.getBarbelClock().now();
    }
    
    @AfterAll
    public static void tearDown() {
        BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
    }

    @Order(11)
    @Test
    public void addSomeMoreData() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        DefaultPojo pojo = new DefaultPojo("someOther", "some data");
        core.save(pojo);
        assertEquals(25, ((BarbelHistoCore<DefaultPojo>)core).size());
    }
}
