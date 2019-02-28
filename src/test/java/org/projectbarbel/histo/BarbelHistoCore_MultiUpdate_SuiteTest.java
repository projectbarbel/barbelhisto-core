package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.projectbarbel.histo.model.DefaultPojo;

@TestMethodOrder(OrderAnnotation.class)
public class BarbelHistoCore_MultiUpdate_SuiteTest {

    static BarbelHisto<DefaultPojo> core = BarbelHistoTestContext.INSTANCE.apply(DefaultPojo.class).build();

    // @formatter:off
    @Order(1)
    @Test
    void embeddedOverlap_Local_1() throws Exception {
        DefaultPojo pojo = new DefaultPojo("someSome", "some data");
        
        // Now |---------------------------------| 20
        core.save(pojo, LocalDate.now(), LocalDate.now().plusDays(20));
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(0, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(2)
    @Test
    void embeddedOverlap_Local_2() throws Exception {
    
        // Now |---------------------------------| 20
        //      1|---------------|10
        //     |-|---------------|---------------| 20
        DefaultPojo pojo = new DefaultPojo("someSome", "changed");
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));
        assertEquals(4,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(1, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(3)
    @Test
    void embeddedOverlap_Local_3() throws Exception {

        //     |-|---------------|---------------| 20
        //      1|-------------------------------| 20
        //     |-|-------------------------------| 20
        DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.now().plusDays(20));
        assertEquals(5,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(3, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
    }
    
    @Order(4)
    @Test
    void embeddedOverlap_Local_4() throws Exception {
        //     |-|-------------------------------| 20
        //      1|-------------------------------| 20
        //     |-|-------------------------------| 20
        DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.now().plusDays(20));
        assertEquals(6,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(4, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
    }
    
    @Order(5)
    @Test
    void embeddedOverlap_Local_5() throws Exception {
        //     |-|-------------------------------| 20
        //     |---------------------------------| 20
        //     |---------------------------------| 20
        DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, LocalDate.now(), LocalDate.now().plusDays(20));
        assertEquals(7,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(6, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(6)
    @Test
    void embeddedOverlap_Local_6() throws Exception {
       //     |---------------------------------| 20
       //     |-----------------| 10
       //     |-----------------|---------------| 20
           DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
       core.save(pojo, LocalDate.now(), LocalDate.now().plusDays(10));
       assertEquals(9,((BarbelHistoCore<DefaultPojo>)core).size());
       assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
       assertEquals(7, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(7)
    @Test
    void embeddedOverlap_Local_7() throws Exception {
        //     |-----------------|---------------| 20
        //     |--------------------------------------------------| 100
        //     |--------------------------------------------------| 100
            DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, LocalDate.now(), LocalDate.now().plusDays(100));
        assertEquals(10,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(9, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(8)
    @Test
    void embeddedOverlap_Local_8() throws Exception {
        //     |---------------------------------------------------| 100
        //     |-|-----------------------------------------------|-| 100
        //     |-|-----------------------------------------------|-| 100
            DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.now().plusDays(99));
        assertEquals(13,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(10, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(9)
    @Test
    void embeddedOverlap_Local_9() throws Exception {
        //     |-|-----------------------------------------------|-| 100
        //       |--| 3
        //     |-|--|--------------------------------------------|-| 100
            DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        assertEquals(15,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(4, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(11, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(10)
    @Test
    void embeddedOverlap_Local_10() throws Exception {
        //     |-|--|--------------------------------------------|-| 100
        //         3|--|5
        //     |-|--|--|-----------------------------------------|-| 100
            DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        assertEquals(17,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(12, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(11)
    @Test
    void embeddedOverlap_Local_11() throws Exception {
        //     |-|--|--|-----------------------------------------|-| 100
        //            5|--|7
        //     |-|--|--|--|--------------------------------------|-| 100
            DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(5), LocalDate.now().plusDays(7));
        assertEquals(19,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(6, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(13, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(12)
    @Test
    void embeddedOverlap_Local_12() throws Exception {
        //     |-|--|--|--|--------------------------------------|-| 100
        //                |----------------------------------------| 100
        //     |-|--|--|--|----------------------------------------| 100
        DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(7), LocalDate.now().plusDays(100));
        assertEquals(20,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(15, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(13)
    @Test
    void embeddedOverlap_Local_13() throws Exception {
        //     |-|--|--|--|----------------------------------------| 100
        //        |------|
        //     |-||------||----------------------------------------| 100
            DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(2), LocalDate.now().plusDays(6));
        assertEquals(23,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(18, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
        
    @Order(14)
    @Test
    void embeddedOverlap_Local_14() throws Exception {
        //     |-||------||----------------------------------------| 100
        //        |-------|
        //     |-||-------|----------------------------------------| 100
        DefaultPojo pojo = new DefaultPojo("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(2), LocalDate.now().plusDays(7));
        assertEquals(24,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(4, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(20, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
        
    @Test
    void embeddedOverlap_Max() throws Exception {
        BarbelHisto<DefaultPojo> core = BarbelHistoTestContext.INSTANCE.apply(DefaultPojo.class).build();
        DefaultPojo pojo = new DefaultPojo("someSome", "some data");
        
        // Now |---------------------------------| MAX
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(0, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        // Now |---------------------------------| MAX
        //      |--------------------------------| Max
        //     ||--------------------------------| Max
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.MAX);
        assertEquals(3,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(1, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
        //     ||--------------------------------| Max
        //        5|-----------------------------| MAX
        //     ||--|-----------------------------| Max
        core.save(pojo, LocalDate.now().plusDays(5), LocalDate.MAX);
        assertEquals(5,((BarbelHistoCore<DefaultPojo>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(2, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
    }
    // @formatter:on

}
