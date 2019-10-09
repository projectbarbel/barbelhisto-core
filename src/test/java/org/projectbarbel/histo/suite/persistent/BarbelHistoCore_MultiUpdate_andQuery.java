package org.projectbarbel.histo.suite.persistent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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
public class BarbelHistoCore_MultiUpdate_andQuery {

    // @formatter:off
    @Order(1)
    @Test
    void embeddedOverlap_Extrapolate() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        DefaultPojo pojo = new DefaultPojo("someSome", "some data");
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        // Now |---------------------------------| 20
        core.save(pojo, now, now.plusDays(20));
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(0, core.retrieve(BarbelQueries.allInactive("someSome")).size());
                
        // Now |---------------------------------| 20
        //      1|---------------|10
        //     |-|---------------|---------------| 20
        pojo = new DefaultPojo("someSome", "changed");
        core.save(pojo, now.plusDays(1), now.plusDays(10));
        assertEquals(4,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(1, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|---------------|---------------| 20
        //      1|-------------------------------| 20
        //     |-|-------------------------------| 20
        pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now.plusDays(1), now.plusDays(20));
        assertEquals(5,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(3, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|-------------------------------| 20
        //      1|-------------------------------| 20
        //     |-|-------------------------------| 20
        core.save(pojo, now.plusDays(1), now.plusDays(20));
        assertEquals(6,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(4, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|-------------------------------| 20
        //     |---------------------------------| 20
        //     |---------------------------------| 20
        core.save(pojo, now, now.plusDays(20));
        assertEquals(7,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(6, core.retrieve(BarbelQueries.allInactive("someSome")).size());

        //     |---------------------------------| 20
        //     |-----------------| 10
        //     |-----------------|---------------| 20
        core.save(pojo, now, now.plusDays(10));
        assertEquals(9,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(7, core.retrieve(BarbelQueries.allInactive("someSome")).size());

        //     |-----------------|---------------| 20
        //     |--------------------------------------------------| 100
        //     |--------------------------------------------------| 100
        core.save(pojo, now, now.plusDays(100));
        assertEquals(10,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(9, core.retrieve(BarbelQueries.allInactive("someSome")).size());

        //     |---------------------------------------------------| 100
        //     |-|-----------------------------------------------|-| 100
        //     |-|-----------------------------------------------|-| 100
        core.save(pojo, now.plusDays(1), now.plusDays(99));
        assertEquals(13,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(10, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|-----------------------------------------------|-| 100
        //       |--| 3
        //     |-|--|--------------------------------------------|-| 100
        core.save(pojo, now.plusDays(1), now.plusDays(3));
        assertEquals(15,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(4, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(11, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|--|--------------------------------------------|-| 100
        //         3|--|5
        //     |-|--|--|-----------------------------------------|-| 100
        core.save(pojo, now.plusDays(3), now.plusDays(5));
        assertEquals(17,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(12, core.retrieve(BarbelQueries.allInactive("someSome")).size());

        //     |-|--|--|-----------------------------------------|-| 100
        //            5|--|7
        //     |-|--|--|--|--------------------------------------|-| 100
        core.save(pojo, now.plusDays(5), now.plusDays(7));
        assertEquals(19,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(6, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(13, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|--|--|--|--------------------------------------|-| 100
        //                |----------------------------------------| 100
        //     |-|--|--|--|----------------------------------------| 100
        core.save(pojo, now.plusDays(7), now.plusDays(100));
        assertEquals(20,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(15, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-|--|--|--|----------------------------------------| 100
        //        |------|
        //     |-||------||----------------------------------------| 100
        core.save(pojo, now.plusDays(2), now.plusDays(6));
        assertEquals(23,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(18, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     |-||------||----------------------------------------| 100
        //        |-------|
        //     |-||-------|----------------------------------------| 100
        core.save(pojo, now.plusDays(2), now.plusDays(7));
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
        assertEquals(2, core.retrieve(BarbelQueries.effectiveAfter("someSome", BarbelHistoContext.getBarbelClock().now().plusDays(2))).size());
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
