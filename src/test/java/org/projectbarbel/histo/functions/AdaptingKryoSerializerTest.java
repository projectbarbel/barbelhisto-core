package org.projectbarbel.histo.functions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.BarbelTestHelper;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructorWithComplexType;
import org.projectbarbel.histo.pojos.NoPrimitivePrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojo;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojoPartialContructor;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;

import io.github.benas.randombeans.api.EnhancedRandom;

public class AdaptingKryoSerializerTest {

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
	public void testValidateObjectIsRoundTripSerializable(Object pojo) throws Exception {
		AdaptingKryoSerializer.validateObjectIsRoundTripSerializable(BarbelHistoBuilder.barbel(), pojo);
	}

    private HashMap<String, Object> options = new HashMap<String, Object>();
    private BarbelHistoContext context = BarbelHistoBuilder.barbel().withContextOptions(options);
    private PersistenceConfig config = new PersistenceConfig() {
        
        @Override
        public Class<? extends Annotation> annotationType() {
            return PersistenceConfig.DEFAULT_CONFIG.annotationType();
        }
        
        @Override
        public Class<? extends PojoSerializer<?>> serializer() {
            return AdaptingKryoSerializer.class;
        }
        
        @Override
        public boolean polymorphic() {
            return true;
        }
    };
    
    @BeforeEach
    public void setUp() {
    }
    
    @Test
    public void testSerialize() throws Exception {
        options.put(AdaptingKryoSerializer.OBJECT_TYPE, DefaultDocument.class);
        options.put(AdaptingKryoSerializer.PERSISTENCE_CONFIG, config);
        AdaptingKryoSerializer serializer = new AdaptingKryoSerializer(context);
        byte[] bytes = serializer.serialize(BarbelTestHelper.random(DefaultDocument.class));
        assertTrue(bytes.length>0);
    }

    @Test
    public void testSerialize_Proxy() throws Exception {
        DefaultPojo pojo = (DefaultPojo)BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(), EnhancedRandom.random(DefaultPojo.class), BitemporalStamp.createActive());
        options.put(AdaptingKryoSerializer.OBJECT_TYPE, DefaultPojo.class);
        options.put(AdaptingKryoSerializer.PERSISTENCE_CONFIG, config);
        AdaptingKryoSerializer serializer = new AdaptingKryoSerializer(context);
        byte[] bytes = serializer.serialize((Bitemporal)pojo);
        assertTrue(bytes.length>0);
    }

    @Test
    public void testDeserialize() throws Exception {
        DefaultPojo pojo = (DefaultPojo)BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(), EnhancedRandom.random(DefaultPojo.class), BitemporalStamp.createActive());
        options.put(AdaptingKryoSerializer.OBJECT_TYPE, DefaultPojo.class);
        options.put(AdaptingKryoSerializer.PERSISTENCE_CONFIG, config);
        AdaptingKryoSerializer serializer = new AdaptingKryoSerializer(context);
        byte[] bytes = serializer.serialize((Bitemporal)pojo);
        DefaultPojo deserialized = (DefaultPojo) serializer.deserialize(bytes);
        assertTrue(deserialized.equals(pojo));
        assertTrue(deserialized instanceof BarbelProxy);
        assertTrue(deserialized instanceof Bitemporal);
    }
	
}
