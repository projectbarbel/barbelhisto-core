package com.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalVersion;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCore_CQPersistence_Test {

    private static final String FILENAME = "test.dat";
    private BarbelHistoCore core;
    public static final SimpleAttribute<BitemporalVersion, String> VERSION_ID_PK = new SimpleAttribute<BitemporalVersion, String>(
            "documentId") {
        public String getValue(BitemporalVersion object, QueryOptions queryOptions) {
            return (String) object.getBitemporalStamp().getVersionId();
        }
    };

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
    public void testSave(Object pojo) throws IOException {
        core = (BarbelHistoCore) BarbelHistoBuilder.barbel()
                .withPersistenceCollection(() -> new ConcurrentIndexedCollection<BitemporalVersion>(
                        DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK, new File(FILENAME))))
                .build();
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        core = (BarbelHistoCore) BarbelHistoBuilder.barbel()
                .withPersistenceCollection(() -> new ConcurrentIndexedCollection<BitemporalVersion>(
                        DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK, new File(FILENAME))))
                .build();
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.MAX);
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.dump();
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
        Files.delete(Paths.get(FILENAME));
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

    @SuppressWarnings("unused")
    public static class ComplexFieldsPrivatePojoPartialContructor {
        @DocumentId
        private String id;
        private List<String> stringList;
        private Map<String, NoPrimitivePrivatePojoPartialContructor> someMap;

        public ComplexFieldsPrivatePojoPartialContructor(String id) {
            this.id = id;
        }
    }

    @SuppressWarnings("unused")
    public static class ComplexFieldsPrivatePojoPartialContructorWithComplexType {
        @DocumentId
        private String id;
        private List<String> stringList;
        private Map<String, NoPrimitivePrivatePojoPartialContructor> someMap;

        public ComplexFieldsPrivatePojoPartialContructorWithComplexType(
                Map<String, NoPrimitivePrivatePojoPartialContructor> someMap) {
            this.someMap = someMap;
        }
    }

}
