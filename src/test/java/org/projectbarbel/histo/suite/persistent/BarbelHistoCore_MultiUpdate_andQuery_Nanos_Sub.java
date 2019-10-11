package org.projectbarbel.histo.suite.persistent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.BarbelQueries;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTNotStandAlone;

import com.googlecode.cqengine.query.QueryFactory;

@BTNotStandAlone
@TestMethodOrder(OrderAnnotation.class)
public class BarbelHistoCore_MultiUpdate_andQuery_Nanos_Sub {

    static ZonedDateTime now;
    
    @BeforeAll
    public static void setup() {
        now = BarbelHistoContext.getBarbelClock().now();
        BarbelHistoContext.getBarbelClock().useFixedClockAt(now);
    }
    
    @AfterAll
    public static void teardown() {
        BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
    }
    
    // @formatter:off
    @Order(1)
    @Test
    void embeddedOverlap_Extrapolate() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        DefaultPojo pojo = new DefaultPojo("someSome", "some data");
        // Now |---------------------------------| 20
        core.save(pojo, now, now.plusNanos(20001234));
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(0, core.retrieve(BarbelQueries.allInactive("someSome")).size());
                
        // Now |---------------------------------| 20
        //      1|---------------|10
        //     |-|---------------|---------------| 20
        pojo = new DefaultPojo("someSome", "changed");
        core.save(pojo, now.plusNanos(1012345), now.plusNanos(10004567));
        assertEquals(4,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(1, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|---------------|---------------| 20
        //      1|-------------------------------| 20
        //     |-|-------------------------------| 20
        pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now.plusNanos(1003456), now.plusNanos(20007777));
        assertEquals(5,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(3, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|-------------------------------| 20
        //      1|-------------------------------| 20
        //     |-|-------------------------------| 20
        core.save(pojo, now.plusNanos(1008765), now.plusNanos(20002345));
        assertEquals(6,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(4, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|-------------------------------| 20
        //     |---------------------------------| 20
        //     |---------------------------------| 20
        core.save(pojo, now, now.plusNanos(20123654));
        assertEquals(7,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(6, core.retrieve(BarbelQueries.allInactive("someSome")).size());

        //     |---------------------------------| 20
        //     |-----------------| 10
        //     |-----------------|---------------| 20
        core.save(pojo, now, now.plusNanos(10234987));
        assertEquals(9,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(7, core.retrieve(BarbelQueries.allInactive("someSome")).size());

        //     |-----------------|---------------| 20
        //     |--------------------------------------------------| 100
        //     |--------------------------------------------------| 100
        core.save(pojo, now, now.plusNanos(100234567));
        assertEquals(10,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(9, core.retrieve(BarbelQueries.allInactive("someSome")).size());

        //     |---------------------------------------------------| 100
        //     |-|-----------------------------------------------|-| 100
        //     |-|-----------------------------------------------|-| 100
        core.save(pojo, now.plusNanos(1987654), now.plusNanos(99345678));
        assertEquals(13,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(10, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|-----------------------------------------------|-| 100
        //       |--| 3
        //     |-|--|--------------------------------------------|-| 100
        core.save(pojo, now.plusNanos(1000345), now.plusNanos(3003459));
        assertEquals(15,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(4, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(11, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|--|--------------------------------------------|-| 100
        //         3|--|5
        //     |-|--|--|-----------------------------------------|-| 100
        core.save(pojo, now.plusNanos(3003456), now.plusNanos(5008798));
        assertEquals(17,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(12, core.retrieve(BarbelQueries.allInactive("someSome")).size());

        //     |-|--|--|-----------------------------------------|-| 100
        //            5|--|7
        //     |-|--|--|--|--------------------------------------|-| 100
        core.save(pojo, now.plusNanos(5003412), now.plusNanos(7023654));
        assertEquals(19,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(6, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(13, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|--|--|--|--------------------------------------|-| 100
        //                |----------------------------------------| 100
        //     |-|--|--|--|----------------------------------------| 100
        core.save(pojo, now.plusNanos(7023765), now.plusNanos(100036456));
        assertEquals(20,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(15, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|--|--|--|----------------------------------------| 100
        //        |------|
        //     |-||------||----------------------------------------| 100
        core.save(pojo, now.plusNanos(2034560), now.plusNanos(6000034));
        assertEquals(23,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(18, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-||------||----------------------------------------| 100
        //        |-------|
        //     |-||-------|----------------------------------------| 100
        core.save(pojo, now.plusNanos(2000347), now.plusNanos(7676545));
        assertEquals(24,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(4, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(20, core.retrieve(BarbelQueries.allInactive("someSome")).size());

    }
    
    @Order(2)
    @Test
    void allOtherQueries_preFetch_AllId() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        assertEquals(24, core.retrieve(BarbelQueries.all("someSome")).size());
    }
    @Order(4)
    @Test
    void allOtherQueries_preFetch_allActive() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        assertEquals(4, core.retrieve(BarbelQueries.allActive("someSome")).size());
    }
    @Order(5)
    @Test
    void allOtherQueries_preFetch_allInactive() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        assertEquals(20, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    @Order(6)
    @Test
    void allOtherQueries_preFetch_effectiveAfter() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        assertEquals(2, core.retrieve(BarbelQueries.effectiveAfter("someSome", now.plusNanos(2263546))).size());
    }
    @Order(7)
    @Test
    void allOtherQueries_preFetch_effectiveBetween() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        assertEquals(4, core.retrieve(BarbelQueries.effectiveBetween("someSome", EffectivePeriod.nowToInfinite())).size());
    }
    @Order(8)
    @Test
    void allOtherQueries_preFetch_effectiveNow() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        System.out.println(core.prettyPrintJournal("someSome"));
        assertEquals(1, core.retrieve(BarbelQueries.effectiveNow("someSome")).size());
    }
    @Order(9)
    @Test
    void allOtherQueries_preFetch_journalAt() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        assertEquals(4, core.retrieve(BarbelQueries.journalAt("someSome", BarbelHistoContext.getBarbelClock().now())).size());
    }
    @Order(10)
    @Test
    void allOtherQueries_preFetch_All() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        assertEquals(24, core.retrieve(BarbelQueries.all()).size());
    }
    @Order(12)
    @Test
    void addLoadingTwoIDs() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        assertEquals(25, core.retrieve(QueryFactory.or(BarbelQueries.all("someSome"), BarbelQueries.all("someOther"))).size());
    }
    @Order(13)
    @Test
    void addLoadingOther() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        assertEquals(1, core.retrieve(BarbelQueries.all("someOther")).size());
    }
    // @formatter:on

}
