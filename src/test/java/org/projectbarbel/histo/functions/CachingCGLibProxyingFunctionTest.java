package org.projectbarbel.histo.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructorWithComplexType;
import org.projectbarbel.histo.pojos.NoPrimitivePrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojo;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojoPartialContructor;

import io.github.benas.randombeans.api.EnhancedRandom;

public class CachingCGLibProxyingFunctionTest {

    private CachingCGLibProxyingFunction proxying = CachingCGLibProxyingFunction.INSTANCE;
    
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

    @SuppressWarnings("unused")
    private static Stream<Arguments> createPojos() {
        return Stream.of(Arguments.of(EnhancedRandom.random(PrimitivePrivatePojo.class)),
                Arguments.of(EnhancedRandom.random(PrimitivePrivatePojoPartialContructor.class)),
                Arguments.of(EnhancedRandom.random(NoPrimitivePrivatePojoPartialContructor.class)),
                Arguments.of(EnhancedRandom.random(ComplexFieldsPrivatePojoPartialContructorWithComplexType.class)),
                Arguments.of(EnhancedRandom.random(ComplexFieldsPrivatePojoPartialContructor.class)));
    }

    @ParameterizedTest
    @MethodSource("createPojos")
    public void testApply_checkEqual(Object pojo) throws Exception {
        BitemporalStamp stamp = BitemporalStamp.createActive();
        Object proxy = proxying.apply(pojo, stamp);
        assertEquals(pojo, ((BarbelProxy)proxy).getTarget());
        assertEquals(stamp, ((Bitemporal)proxy).getBitemporalStamp());
    }
}
