package org.projectbarbel.histo.suite.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.BarbelQueries;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTTestStandard;

@ExtendWith(BTTestStandard.class)
@TestMethodOrder(OrderAnnotation.class)
public class BarbelHistoCore_MultiUpdate_SuiteTest {

    static BarbelHisto<DefaultPojo> core;

    @BeforeAll
    public static void setUo() {
        core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
	    BarbelHistoContext.getBarbelClock().useFixedClockAt(LocalDateTime.now().atZone(ZoneId.of("Z")));
    }

    @AfterAll
    public static void restartClock() {
        BarbelHistoContext.getBarbelClock().useSystemDefaultZoneClock();
    }

    // @formatter:off
    @Order(1)
    @Test
    void update_1() throws Exception {
        DefaultPojo pojo = new DefaultPojo("someSome", "some data");
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        // Now |---------------------------------| 20
        core.save(pojo, now, now.plusDays(20));
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(0, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }

    @Order(2)
    @Test
    void update_2() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        // Now |---------------------------------| 20
        //      1|---------------|10
        //     |-|---------------|---------------| 20
        DefaultPojo pojo = new DefaultPojo("someSome", "changed");
        core.save(pojo, now.plusDays(1), now.plusDays(10));
        assertEquals(4,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(1, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }

    @Order(3)
    @Test
    void update_3() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        //     |-|---------------|---------------| 20
        //      1|-------------------------------| 20
        //     |-|-------------------------------| 20
        DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now.plusDays(1), now.plusDays(20));
        assertEquals(5,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(3, core.retrieve(BarbelQueries.allInactive("someSome")).size());

    }

    @Order(4)
    @Test
    void update_4() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        //     |-|-------------------------------| 20
        //      1|-------------------------------| 20
        //     |-|-------------------------------| 20
        DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now.plusDays(1), now.plusDays(20));
        assertEquals(6,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(4, core.retrieve(BarbelQueries.allInactive("someSome")).size());

    }

    @Order(5)
    @Test
    void update_5() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        //     |-|-------------------------------| 20
        //     |---------------------------------| 20
        //     |---------------------------------| 20
        DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now, now.plusDays(20));
        assertEquals(7,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(6, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }

    @Order(6)
    @Test
    void update_6() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        //     |---------------------------------| 20
       //     |-----------------| 10
       //     |-----------------|---------------| 20
           DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
       core.save(pojo, now, now.plusDays(10));
       assertEquals(9,((BarbelHistoCore<DefaultPojo>)core).size());
       assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
       assertEquals(7, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }

    @Order(7)
    @Test
    void update_7() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        //     |-----------------|---------------| 20
        //     |--------------------------------------------------| 100
        //     |--------------------------------------------------| 100
            DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now, now.plusDays(100));
        assertEquals(10,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(9, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }

    @Order(8)
    @Test
    void update_8() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        //     |---------------------------------------------------| 100
        //     |-|-----------------------------------------------|-| 100
        //     |-|-----------------------------------------------|-| 100
        DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now.plusDays(1), now.plusDays(99));
        assertEquals(13,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(10, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }

    @Order(9)
    @Test
    void update_9() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        //     |-|-----------------------------------------------|-| 100
        //       |--| 3
        //     |-|--|--------------------------------------------|-| 100
            DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now.plusDays(1), now.plusDays(3));
        assertEquals(15,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(4, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(11, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }

    @Order(10)
    @Test
    void update_10() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        //     |-|--|--------------------------------------------|-| 100
        //         3|--|5
        //     |-|--|--|-----------------------------------------|-| 100
            DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now.plusDays(3), now.plusDays(5));
        assertEquals(17,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(12, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }

    @Order(11)
    @Test
    void update_11() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        //     |-|--|--|-----------------------------------------|-| 100
        //            5|--|7
        //     |-|--|--|--|--------------------------------------|-| 100
            DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now.plusDays(5), now.plusDays(7));
        assertEquals(19,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(6, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(13, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }

    @Order(12)
    @Test
    void update_12() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        //     |-|--|--|--|--------------------------------------|-| 100
        //                |----------------------------------------| 100
        //     |-|--|--|--|----------------------------------------| 100
        DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now.plusDays(7), now.plusDays(100));
        assertEquals(20,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(15, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }

    @Order(13)
    @Test
    void update_13() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        //     |-|--|--|--|----------------------------------------| 100
        //        |------|
        //     |-||------||----------------------------------------| 100
            DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now.plusDays(2), now.plusDays(6));
        assertEquals(23,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(18, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }

    @Order(14)
    @Test
    void update_14() throws Exception {
        ZonedDateTime now = BarbelHistoContext.getBarbelClock().now();
        //     |-||------||----------------------------------------| 100
        //        |-------|
        //     |-||-------|----------------------------------------| 100
        DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, now.plusDays(2), now.plusDays(7));
        assertEquals(24,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(4, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(20, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    // @formatter:on

}
