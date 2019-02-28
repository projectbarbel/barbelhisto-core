package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructorWithComplexType;
import org.projectbarbel.histo.pojos.NoPrimitivePrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojo;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojoPartialContructor;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCore_Pojo_Test {

    @SuppressWarnings("unused")
    private static Stream<Arguments> createPojos() {
        return Stream.of(
                Arguments.of(EnhancedRandom.random(PrimitivePrivatePojo.class)),
                Arguments.of(EnhancedRandom.random(PrimitivePrivatePojoPartialContructor.class)),
                Arguments.of(EnhancedRandom.random(NoPrimitivePrivatePojoPartialContructor.class)),
                Arguments.of(EnhancedRandom.random(ComplexFieldsPrivatePojoPartialContructorWithComplexType.class)),
                Arguments.of(EnhancedRandom.random(ComplexFieldsPrivatePojoPartialContructor.class))
                );
    }
    
    @ParameterizedTest
    @MethodSource("createPojos")
    public <T> void testSave(T pojo) {
        BarbelHisto<T> core = BarbelHistoTestContext.INSTANCE.apply(pojo.getClass()).build();
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        assertEquals(1, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal)core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
    }
    
}
