package org.projectbarbel.histo.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojoPartialContructor;

import io.github.benas.randombeans.api.EnhancedRandom;

public class CGLibProxyingFunctionTest {

    private CGLibProxyingFunction proxying = new CGLibProxyingFunction();
    
    @Test
    public void testApply_setBitemporal() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalStamp stamp1 = BitemporalStamp.createActive();
        Bitemporal proxy = (Bitemporal)proxying.apply(pojo, stamp1);
        assertEquals(stamp1, proxy.getBitemporalStamp());
        BitemporalStamp stamp2 = BitemporalStamp.createActive();
        proxy.setBitemporalStamp(stamp2);
        assertEquals(stamp2, proxy.getBitemporalStamp());
    }

    @Test
    public void testApply_setBitemporal_null() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalStamp stamp1 = BitemporalStamp.createActive();
        Bitemporal proxy = (Bitemporal)proxying.apply(pojo, stamp1);
        assertEquals(stamp1, proxy.getBitemporalStamp());
        BitemporalStamp stampNull = null;
        proxy.setBitemporalStamp(stampNull);
        assertEquals(null, proxy.getBitemporalStamp());
    }
    
    @Test
    public void testApply_getTargetPojo() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalStamp stamp = BitemporalStamp.createActive();
        DefaultPojo proxy = (DefaultPojo)proxying.apply(pojo, stamp);
        assertEquals(pojo, ((BarbelProxy)proxy).getTarget());
    }

    @Test
    public void testApply_getPojoPrimitivePartial() throws Exception {
        PrimitivePrivatePojoPartialContructor pojo = EnhancedRandom.random(PrimitivePrivatePojoPartialContructor.class);
        BitemporalStamp stamp = BitemporalStamp.createActive();
        PrimitivePrivatePojoPartialContructor proxy = (PrimitivePrivatePojoPartialContructor)proxying.apply(pojo, stamp);
        assertEquals(pojo, ((BarbelProxy)proxy).getTarget());
        assertFalse(pojo.equals(proxy));
        assertTrue(pojo.hashCode()!=proxy.hashCode());
        assertNotNull(((Bitemporal)proxy).getBitemporalStamp());
        BitemporalStamp bs = BitemporalStamp.createActive();
        ((Bitemporal)proxy).setBitemporalStamp(bs);
        assertEquals(bs, ((Bitemporal)proxy).getBitemporalStamp());
        assertNotNull(proxy.toString());
        ((BarbelProxy)proxy).setTarget(new Object());
    }
    
    @Test
    public void testApply_getBitemporal() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalStamp stamp = BitemporalStamp.createActive();
        DefaultPojo proxy = (DefaultPojo)proxying.apply(pojo, stamp);
        assertEquals(stamp, ((Bitemporal)proxy).getBitemporalStamp());
    }

    @Test
    public void testApply_checkData() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalStamp stamp = BitemporalStamp.createActive();
        DefaultPojo proxy = (DefaultPojo)proxying.apply(pojo, stamp);
        assertEquals(pojo.getData(), proxy.getData());
        assertEquals(pojo.getDocumentId(), proxy.getDocumentId());
    }

}
