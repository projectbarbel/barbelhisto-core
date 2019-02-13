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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.functions.BarbelPojoSerializer;
import com.projectbarbel.histo.model.Bitemporal;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCore_CQPersistence_Test {

    private static final String FILENAME = "test.dat";
    
    final SimpleAttribute<PrimitivePrivatePojo, String> VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO = new SimpleAttribute<PrimitivePrivatePojo, String>(
            "documentId") {
        public String getValue(PrimitivePrivatePojo object, QueryOptions queryOptions) {
            return (String) ((Bitemporal)object).getBitemporalStamp().getVersionId();
        }
    };

    final SimpleAttribute<PrimitivePrivatePojoPartialContructor, String> VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO_PARTIAL = new SimpleAttribute<PrimitivePrivatePojoPartialContructor, String>(
            "documentId") {
        public String getValue(PrimitivePrivatePojoPartialContructor object, QueryOptions queryOptions) {
            return (String) ((Bitemporal)object).getBitemporalStamp().getVersionId();
        }
    };
    
    final SimpleAttribute<NoPrimitivePrivatePojoPartialContructor, String> VERSION_ID_PK_NO_PRIMITIVE_PRIVATE_POJO_PARTIAL = new SimpleAttribute<NoPrimitivePrivatePojoPartialContructor, String>(
            "documentId") {
        public String getValue(NoPrimitivePrivatePojoPartialContructor object, QueryOptions queryOptions) {
            return (String) ((Bitemporal)object).getBitemporalStamp().getVersionId();
        }
    };
    
    final SimpleAttribute<ComplexFieldsPrivatePojoPartialContructor, String> ComplexFieldsPrivatePojoPartialContructor_Field = new SimpleAttribute<ComplexFieldsPrivatePojoPartialContructor, String>(
            "documentId") {
        public String getValue(ComplexFieldsPrivatePojoPartialContructor object, QueryOptions queryOptions) {
            return (String) ((Bitemporal)object).getBitemporalStamp().getVersionId();
        }
    };
    
    final SimpleAttribute<ComplexFieldsPrivatePojoPartialContructorWithComplexType, String> ComplexFieldsPrivatePojoPartialContructorWithComplexType_Field = new SimpleAttribute<ComplexFieldsPrivatePojoPartialContructorWithComplexType, String>(
            "documentId") {
        public String getValue(ComplexFieldsPrivatePojoPartialContructorWithComplexType object, QueryOptions queryOptions) {
            return (String) ((Bitemporal)object).getBitemporalStamp().getVersionId();
        }
    };
    
    @AfterEach
    public void tearDown() throws IOException {
        Files.delete(Paths.get(FILENAME));
        Files.delete(Paths.get(FILENAME+"-shm"));
        Files.delete(Paths.get(FILENAME+"-wal"));
    }
    
    @SuppressWarnings("unused")
    private static Stream<Arguments> createPojos() {
        return Stream.of(Arguments.of(EnhancedRandom.random(PrimitivePrivatePojo.class)),
                Arguments.of(EnhancedRandom.random(PrimitivePrivatePojoPartialContructor.class)),
                Arguments.of(EnhancedRandom.random(NoPrimitivePrivatePojoPartialContructor.class)),
                Arguments.of(EnhancedRandom.random(ComplexFieldsPrivatePojoPartialContructorWithComplexType.class)),
                Arguments.of(EnhancedRandom.random(ComplexFieldsPrivatePojoPartialContructor.class)));
    }

    @Test
    public void testSave_PrimitivePrivatePojo() throws IOException {
        PrimitivePrivatePojo pojo = EnhancedRandom.random(PrimitivePrivatePojo.class);
        BarbelHisto<PrimitivePrivatePojo> core = BarbelHistoBuilder.barbel()
                .withBackbone(new ConcurrentIndexedCollection<PrimitivePrivatePojo>(
                        DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO, new File(FILENAME))))
                .build();
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        core = BarbelHistoBuilder.barbel()
                .withBackbone(new ConcurrentIndexedCollection<PrimitivePrivatePojo>(
                        DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO, new File(FILENAME))))
                .build();
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.MAX);
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.dump();
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
    }

    @Test
    public void testSave_PrimitivePrivatePojoPartialContructor() throws IOException {
        PrimitivePrivatePojoPartialContructor pojo = EnhancedRandom.random(PrimitivePrivatePojoPartialContructor.class);
        BarbelHisto<PrimitivePrivatePojoPartialContructor> core = BarbelHistoBuilder.barbel()
                .withBackbone(new ConcurrentIndexedCollection<PrimitivePrivatePojoPartialContructor>(
                        DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO_PARTIAL, new File(FILENAME))))
                .build();
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        core = BarbelHistoBuilder.barbel()
                .withBackbone(new ConcurrentIndexedCollection<PrimitivePrivatePojoPartialContructor>(
                        DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO_PARTIAL, new File(FILENAME))))
                .build();
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.MAX);
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.dump();
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
    }
    
    @Test
    public void testSave_NoPrimitivePrivatePojoPartialContructor() throws IOException {
        NoPrimitivePrivatePojoPartialContructor pojo = EnhancedRandom.random(NoPrimitivePrivatePojoPartialContructor.class);
        BarbelHisto<NoPrimitivePrivatePojoPartialContructor> core = BarbelHistoBuilder.barbel()
                .withBackbone(new ConcurrentIndexedCollection<NoPrimitivePrivatePojoPartialContructor>(
                        DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK_NO_PRIMITIVE_PRIVATE_POJO_PARTIAL, new File(FILENAME))))
                .build();
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        core = BarbelHistoBuilder.barbel()
                .withBackbone(new ConcurrentIndexedCollection<NoPrimitivePrivatePojoPartialContructor>(
                        DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK_NO_PRIMITIVE_PRIVATE_POJO_PARTIAL, new File(FILENAME))))
                .build();
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.MAX);
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.dump();
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
    }
    
    @Test
    public void testSave_ComplexFieldsPrivatePojoPartialContructor() throws IOException {
        ComplexFieldsPrivatePojoPartialContructor pojo = EnhancedRandom.random(ComplexFieldsPrivatePojoPartialContructor.class);
        BarbelHisto<ComplexFieldsPrivatePojoPartialContructor> core = BarbelHistoBuilder.barbel()
                .withBackbone(new ConcurrentIndexedCollection<ComplexFieldsPrivatePojoPartialContructor>(
                        DiskPersistence.onPrimaryKeyInFile(ComplexFieldsPrivatePojoPartialContructor_Field, new File(FILENAME))))
                .build();
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        core = BarbelHistoBuilder.barbel()
                .withBackbone(new ConcurrentIndexedCollection<ComplexFieldsPrivatePojoPartialContructor>(
                        DiskPersistence.onPrimaryKeyInFile(ComplexFieldsPrivatePojoPartialContructor_Field, new File(FILENAME))))
                .build();
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.MAX);
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.dump();
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
    }
    
    @Test
    public void testSave_ComplexFieldsPrivatePojoPartialContructorWithComplexType() throws IOException {
        ComplexFieldsPrivatePojoPartialContructorWithComplexType pojo = EnhancedRandom.random(ComplexFieldsPrivatePojoPartialContructorWithComplexType.class);
        BarbelHisto<ComplexFieldsPrivatePojoPartialContructorWithComplexType> core = BarbelHistoBuilder.barbel()
                .withBackbone(new ConcurrentIndexedCollection<ComplexFieldsPrivatePojoPartialContructorWithComplexType>(
                        DiskPersistence.onPrimaryKeyInFile(ComplexFieldsPrivatePojoPartialContructorWithComplexType_Field, new File(FILENAME))))
                .build();
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        core = BarbelHistoBuilder.barbel()
                .withBackbone(new ConcurrentIndexedCollection<ComplexFieldsPrivatePojoPartialContructorWithComplexType>(
                        DiskPersistence.onPrimaryKeyInFile(ComplexFieldsPrivatePojoPartialContructorWithComplexType_Field, new File(FILENAME))))
                .build();
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.MAX);
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.dump();
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
    }
    
    @SuppressWarnings("unused")
    @PersistenceConfig(serializer=BarbelPojoSerializer.class)
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
    @PersistenceConfig(serializer=BarbelPojoSerializer.class)
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
    @PersistenceConfig(serializer=BarbelPojoSerializer.class)
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
    @PersistenceConfig(serializer=BarbelPojoSerializer.class)
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
    @PersistenceConfig(serializer=BarbelPojoSerializer.class)
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
