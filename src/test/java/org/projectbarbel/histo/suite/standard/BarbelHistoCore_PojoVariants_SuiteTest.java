package org.projectbarbel.histo.suite.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelQueries;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.pojos.Adress;
import org.projectbarbel.histo.pojos.BankAccount;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructorWithComplexType;
import org.projectbarbel.histo.pojos.Driver;
import org.projectbarbel.histo.pojos.NoPrimitivePrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.Partner;
import org.projectbarbel.histo.pojos.Policy;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojo;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.RegisteredKeeper;
import org.projectbarbel.histo.pojos.Risk;
import org.projectbarbel.histo.pojos.Vehicle;
import org.projectbarbel.histo.pojos.VehicleUsage;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTC_Standard;

import io.github.benas.randombeans.api.EnhancedRandom;

@ExtendWith(BTC_Standard.class)
public class BarbelHistoCore_PojoVariants_SuiteTest {

    @BeforeEach
    public void setUp() {
        BTExecutionContext.INSTANCE.clearResources();
    }
    
    @SuppressWarnings("unused")
    private static Stream<Arguments> createPojos() {
        return Stream.of(Arguments.of(EnhancedRandom.random(PrimitivePrivatePojo.class)),
                Arguments.of(EnhancedRandom.random(PrimitivePrivatePojoPartialContructor.class)),
                Arguments.of(EnhancedRandom.random(NoPrimitivePrivatePojoPartialContructor.class)),
                Arguments.of(EnhancedRandom.random(ComplexFieldsPrivatePojoPartialContructorWithComplexType.class)),
                Arguments.of(EnhancedRandom.random(ComplexFieldsPrivatePojoPartialContructor.class)),
                Arguments.of(EnhancedRandom.random(Adress.class)), Arguments.of(EnhancedRandom.random(Driver.class)),
                Arguments.of(EnhancedRandom.random(Vehicle.class)),
                Arguments.of(EnhancedRandom.random(RegisteredKeeper.class)),
                Arguments.of(EnhancedRandom.random(Risk.class)), Arguments.of(EnhancedRandom.random(Policy.class)),
                Arguments.of(EnhancedRandom.random(BankAccount.class)),
                Arguments.of(EnhancedRandom.random(Partner.class)),
                Arguments.of(EnhancedRandom.random(VehicleUsage.class)));
    }

    @ParameterizedTest
    @MethodSource("createPojos")
    public <T> void testSave(T pojo) {
        BarbelHisto<T> core = BTExecutionContext.INSTANCE.barbel(pojo.getClass()).build();
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        assertEquals(1, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
    }

}
