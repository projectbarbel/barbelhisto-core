package com.projectbarbel.histo.journal.functions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class CGIPojoProxyingFunctionTest {

    private CGIPojoProxyingFunction<DefaultPojo> proxying = new CGIPojoProxyingFunction<DefaultPojo>();
    
    @Test
    public void testApply_setBitemporal() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalStamp stamp1 = BitemporalStamp.defaultValues();
        Bitemporal proxy = (Bitemporal)proxying.apply(pojo, stamp1);
        assertEquals(stamp1, proxy.getBitemporalStamp());
        BitemporalStamp stamp2 = BitemporalStamp.defaultValues();
        proxy.setBitemporalStamp(stamp2);
        assertEquals(stamp2, proxy.getBitemporalStamp());
    }

    @Test
    public void testApply_setBitemporal_null() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalStamp stamp1 = BitemporalStamp.defaultValues();
        Bitemporal proxy = (Bitemporal)proxying.apply(pojo, stamp1);
        assertEquals(stamp1, proxy.getBitemporalStamp());
        BitemporalStamp stampNull = null;
        proxy.setBitemporalStamp(stampNull);
        assertEquals(null, proxy.getBitemporalStamp());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testApply_getTargetPojo() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalStamp stamp = BitemporalStamp.defaultValues();
        DefaultPojo proxy = proxying.apply(pojo, stamp);
        assertEquals(pojo, ((BarbelProxy<DefaultPojo>)proxy).getTarget());
    }

    @Test
    public void testApply_getBitemporal() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalStamp stamp = BitemporalStamp.defaultValues();
        DefaultPojo proxy = proxying.apply(pojo, stamp);
        assertEquals(stamp, ((Bitemporal)proxy).getBitemporalStamp());
    }

    @Test
    public void testApply_checkData() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalStamp stamp = BitemporalStamp.defaultValues();
        DefaultPojo proxy = proxying.apply(pojo, stamp);
        assertEquals(pojo.getData(), proxy.getData());
        assertEquals(pojo.getDocumentId(), proxy.getDocumentId());
    }

}