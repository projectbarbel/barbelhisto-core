package org.projectbarbel.histo.suite.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.BarbelQueries;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTTestStandard;

@ExtendWith(BTTestStandard.class)
@TestMethodOrder(OrderAnnotation.class)
public class BarbelHistoCore_MultiUpdate_Bitemporal_SuiteTest {

    static BarbelHisto<DefaultDocument> core;

    @BeforeAll
    public static void setUo() {
        core = BTExecutionContext.INSTANCE.barbel(DefaultDocument.class).withMode(BarbelMode.BITEMPORAL).build();
    }
    
    // @formatter:off
    @Order(1)
    @Test
    void update_1() throws Exception {
        DefaultDocument pojo = new DefaultDocument("someSome", "some data");
        
        // Now |---------------------------------| 20
        core.save(pojo, LocalDate.now(), LocalDate.now().plusDays(20));
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(0, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(2)
    @Test
    void update_2() throws Exception {
    
        // Now |---------------------------------| 20
        //      1|---------------|10
        //     |-|---------------|---------------| 20
        DefaultDocument pojo = new DefaultDocument("someSome", "changed");
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.now().plusDays(10));
        assertEquals(4,((BarbelHistoCore<DefaultDocument>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(1, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(3)
    @Test
    void update_3() throws Exception {

        //     |-|---------------|---------------| 20
        //      1|-------------------------------| 20
        //     |-|-------------------------------| 20
        DefaultDocument pojo = new DefaultDocument("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.now().plusDays(20));
        assertEquals(5,((BarbelHistoCore<DefaultDocument>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(3, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
    }
    
    @Order(4)
    @Test
    void update_4() throws Exception {
        //     |-|-------------------------------| 20
        //      1|-------------------------------| 20
        //     |-|-------------------------------| 20
        DefaultDocument pojo = new DefaultDocument("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.now().plusDays(20));
        assertEquals(6,((BarbelHistoCore<DefaultDocument>)core).size());
        assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(4, core.retrieve(BarbelQueries.allInactive("someSome")).size());
        
    }
    
    @Order(5)
    @Test
    void update_5() throws Exception {
        //     |-|-------------------------------| 20
        //     |---------------------------------| 20
        //     |---------------------------------| 20
        DefaultDocument pojo = new DefaultDocument("someSome", "changed again");
        core.save(pojo, LocalDate.now(), LocalDate.now().plusDays(20));
        assertEquals(7,((BarbelHistoCore<DefaultDocument>)core).size());
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(6, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(6)
    @Test
    void update_6() throws Exception {
       //     |---------------------------------| 20
       //     |-----------------| 10
       //     |-----------------|---------------| 20
       DefaultDocument pojo = new DefaultDocument("someSome", "changed again");
       core.save(pojo, LocalDate.now(), LocalDate.now().plusDays(10));
       assertEquals(9,((BarbelHistoCore<DefaultDocument>)core).size());
       assertEquals(2, core.retrieve(BarbelQueries.allActive("someSome")).size());
       assertEquals(7, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(7)
    @Test
    void update_7() throws Exception {
        //     |-----------------|---------------| 20
        //     |--------------------------------------------------| 100
        //     |--------------------------------------------------| 100
        DefaultDocument pojo = new DefaultDocument("someSome", "changed again");
        core.save(pojo, LocalDate.now(), LocalDate.now().plusDays(100));
        assertEquals(10,((BarbelHistoCore<DefaultDocument>)core).size());
        assertEquals(1, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(9, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(8)
    @Test
    void update_8() throws Exception {
        //     |---------------------------------------------------| 100
        //     |-|-----------------------------------------------|-| 100
        //     |-|-----------------------------------------------|-| 100
        DefaultDocument pojo = new DefaultDocument("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.now().plusDays(99));
        assertEquals(13,((BarbelHistoCore<DefaultDocument>)core).size());
        assertEquals(3, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(10, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(9)
    @Test
    void update_9() throws Exception {
        //     |-|-----------------------------------------------|-| 100
        //       |--| 3
        //     |-|--|--------------------------------------------|-| 100
        DefaultDocument pojo = new DefaultDocument("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        assertEquals(15,((BarbelHistoCore<DefaultDocument>)core).size());
        assertEquals(4, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(11, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(10)
    @Test
    void update_10() throws Exception {
        //     |-|--|--------------------------------------------|-| 100
        //         3|--|5
        //     |-|--|--|-----------------------------------------|-| 100
        DefaultDocument pojo = new DefaultDocument("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        assertEquals(17,((BarbelHistoCore<DefaultDocument>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(12, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(11)
    @Test
    void update_11() throws Exception {
        //     |-|--|--|-----------------------------------------|-| 100
        //            5|--|7
        //     |-|--|--|--|--------------------------------------|-| 100
        DefaultDocument pojo = new DefaultDocument("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(5), LocalDate.now().plusDays(7));
        assertEquals(19,((BarbelHistoCore<DefaultDocument>)core).size());
        assertEquals(6, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(13, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(12)
    @Test
    void update_12() throws Exception {
        //     |-|--|--|--|--------------------------------------|-| 100
        //                |----------------------------------------| 100
        //     |-|--|--|--|----------------------------------------| 100
        DefaultDocument pojo = new DefaultDocument("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(7), LocalDate.now().plusDays(100));
        assertEquals(20,((BarbelHistoCore<DefaultDocument>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(15, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    
    @Order(13)
    @Test
    void update_13() throws Exception {
        //     |-|--|--|--|----------------------------------------| 100
        //        |------|
        //     |-||------||----------------------------------------| 100
        DefaultDocument pojo = new DefaultDocument("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(2), LocalDate.now().plusDays(6));
        assertEquals(23,((BarbelHistoCore<DefaultDocument>)core).size());
        assertEquals(5, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(18, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
        
    @Order(14)
    @Test
    void update_14() throws Exception {
        //     |-||------||----------------------------------------| 100
        //        |-------|
        //     |-||-------|----------------------------------------| 100
        DefaultDocument pojo = new DefaultDocument("someSome", "changed again");
        core.save(pojo, LocalDate.now().plusDays(2), LocalDate.now().plusDays(7));
        assertEquals(24,((BarbelHistoCore<DefaultDocument>)core).size());
        assertEquals(4, core.retrieve(BarbelQueries.allActive("someSome")).size());
        assertEquals(20, core.retrieve(BarbelQueries.allInactive("someSome")).size());
    }
    // @formatter:on

}
