package com.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.projectbarbel.histo.model.Bitemporal;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCore_Pojo_Test {

    private BarbelHistoCore core;

    @BeforeEach
    public void setup() {
        core = (BarbelHistoCore) BarbelHistoBuilder.barbel().build();
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> createPojos() {
        return Stream.of(
                Arguments.of(EnhancedRandom.random(PrimitivePrivatePojo.class)),
                Arguments.of(EnhancedRandom.random(PrimitivePrivatePojoPartialContructor.class)),
                Arguments.of(EnhancedRandom.random(NoPrimitivePrivatePojoPartialContructor.class))
                );
    }
    
    @SuppressWarnings("unused")
    private static Stream<Arguments> nullableParameters() {
        return Stream.of(
                Arguments.of(EnhancedRandom.random(PrimitivePrivatePojo.class), LocalDate.now(), null),
                Arguments.of(EnhancedRandom.random(PrimitivePrivatePojo.class), null, LocalDate.now()),
                Arguments.of(null, LocalDate.now(), LocalDate.MAX),
                Arguments.of(new PrimitivePrivatePojo(), LocalDate.now(), LocalDate.MAX)
                );
    }
    
    @Test
    public void testNotAnnotated() {
        assertThrows(IllegalArgumentException.class, ()->core.save("dumb", LocalDate.now(), LocalDate.MAX));
    }

    @ParameterizedTest
    @MethodSource("nullableParameters")
    public void testSaveParameter(Object pojo, LocalDate from, LocalDate until) {
        assertThrows(IllegalArgumentException.class, ()->core.save(pojo, from, until));
    }
    
    @ParameterizedTest
    @MethodSource("createPojos")
    public void testSave(Object pojo) {
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        assertEquals(core.retrieve(BarbelQueries.all()).stream().count(),1);
        Bitemporal record = (Bitemporal)core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
    }
    
    @SuppressWarnings("unused")
    public static class PrimitivePrivatePojo {
        @DocumentId
        private String id;
        private boolean someBoolean;
        private byte somByte;
        private short someShort;
        private char someChar;
        private int someInt;
        private float someFloat;
        private long someLong;
        private double someDouble;
    }

    @SuppressWarnings("unused")
    public static class PrimitivePrivatePojoPartialContructor {
        @DocumentId
        private String id;
        private boolean someBoolean;
        private byte somByte;
        private short someShort;
        private char someChar;
        private int someInt;
        private float someFloat;
        private long someLong;
        private double someDouble;
        public PrimitivePrivatePojoPartialContructor(String id, boolean someBoolean, char someChar, float someFloat,
                double someDouble) {
            super();
            this.id = id;
            this.someBoolean = someBoolean;
            this.someChar = someChar;
            this.someFloat = someFloat;
            this.someDouble = someDouble;
        }
    }
    
    @SuppressWarnings("unused")
    public static class NoPrimitivePrivatePojoPartialContructor {
        @DocumentId
        private String id;
        private boolean someBoolean;
        private byte somByte;
        private short someShort;
        private char someChar;
        private int someInt;
        private float someFloat;
        private long someLong;
        private double someDouble;
        public NoPrimitivePrivatePojoPartialContructor(String id) {
            this.id = id;
        }
    }
    
}
