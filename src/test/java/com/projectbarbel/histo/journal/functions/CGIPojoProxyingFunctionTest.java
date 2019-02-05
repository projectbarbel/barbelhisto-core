package com.projectbarbel.histo.journal.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.projectbarbel.histo.journal.functions.BarbelProxy;
import com.projectbarbel.histo.journal.functions.CGIPojoProxyingFunction;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultPojo;

import io.github.benas.randombeans.api.EnhancedRandom;

public class CGIPojoProxyingFunctionTest {

    private CGIPojoProxyingFunction<DefaultPojo> proxying = new CGIPojoProxyingFunction<DefaultPojo>();
    
    @Test
    public void testApply() throws Exception {
        assertNotNull(proxying.apply(EnhancedRandom.random(DefaultPojo.class), BitemporalStamp.defaultValues()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testApply_getTargetPojo() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalStamp stamp = BitemporalStamp.defaultValues();
        DefaultPojo proxy = proxying.apply(pojo, stamp);
        assertEquals(pojo, ((BarbelProxy<DefaultPojo>)proxy).getTarget());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testApply_getBitemporal() throws Exception {
        DefaultPojo pojo = EnhancedRandom.random(DefaultPojo.class);
        BitemporalStamp stamp = BitemporalStamp.defaultValues();
        DefaultPojo proxy = proxying.apply(pojo, stamp);
        assertEquals(stamp, ((Bitemporal<DefaultPojo>)proxy).getBitemporalStamp());
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
