package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructorWithComplexType;
import org.projectbarbel.histo.pojos.NoPrimitivePrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojo;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojoPartialContructor;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.query.option.QueryOptions;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCore_CQPersistence_Test {

    private static final String FILENAME = "test.dat";

    final SimpleAttribute<PrimitivePrivatePojo, String> VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO = new SimpleAttribute<PrimitivePrivatePojo, String>(
            "versionId") {
        public String getValue(PrimitivePrivatePojo object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    };

    final SimpleAttribute<PrimitivePrivatePojoPartialContructor, String> VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO_PARTIAL = new SimpleAttribute<PrimitivePrivatePojoPartialContructor, String>(
            "versionId") {
        public String getValue(PrimitivePrivatePojoPartialContructor object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    };

    final SimpleAttribute<NoPrimitivePrivatePojoPartialContructor, String> VERSION_ID_PK_NO_PRIMITIVE_PRIVATE_POJO_PARTIAL = new SimpleAttribute<NoPrimitivePrivatePojoPartialContructor, String>(
            "versionId") {
        public String getValue(NoPrimitivePrivatePojoPartialContructor object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    };

    final SimpleAttribute<ComplexFieldsPrivatePojoPartialContructor, String> ComplexFieldsPrivatePojoPartialContructor_Field = new SimpleAttribute<ComplexFieldsPrivatePojoPartialContructor, String>(
            "versionId") {
        public String getValue(ComplexFieldsPrivatePojoPartialContructor object, QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    };

    final SimpleAttribute<ComplexFieldsPrivatePojoPartialContructorWithComplexType, String> ComplexFieldsPrivatePojoPartialContructorWithComplexType_Field = new SimpleAttribute<ComplexFieldsPrivatePojoPartialContructorWithComplexType, String>(
            "versionId") {
        public String getValue(ComplexFieldsPrivatePojoPartialContructorWithComplexType object,
                QueryOptions queryOptions) {
            return (String) ((Bitemporal) object).getBitemporalStamp().getVersionId();
        }
    };

    @AfterEach
    public void tearDown() throws IOException {
        Optional.ofNullable(pers).ifPresent(pers->pers.getFile().delete());
    }

    @BeforeEach
    public void setUp() throws IOException {
        Optional.ofNullable(pers).ifPresent(pers->pers.getFile().delete());
    }

    DiskPersistence<PrimitivePrivatePojo, ?> pers;
    
    @Test
    public void testSave_PrimitivePrivatePojo() throws IOException {
        PrimitivePrivatePojo pojo = EnhancedRandom.random(PrimitivePrivatePojo.class);
        pers = DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO, new File(FILENAME));
        ConcurrentIndexedCollection<PrimitivePrivatePojo> col = new ConcurrentIndexedCollection<PrimitivePrivatePojo>(pers);
        col.clear();
        BarbelHisto<PrimitivePrivatePojo> core = BarbelHistoBuilder.barbel()
                .withBackboneSupplier(() -> col)
                .build();
        core.save(pojo);
        core = BarbelHistoBuilder.barbel()
                .withBackboneSupplier(() -> new ConcurrentIndexedCollection<PrimitivePrivatePojo>(
                        DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO, new File(FILENAME))))
                .build();
        core.save(pojo, BarbelHistoContext.getBarbelClock().now().plusDays(1), EffectivePeriod.INFINITE);
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.unload(pojo.id);
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
    }

    @Test
    public void testSave_PrimitivePrivatePojo_withUpdate() throws IOException {
        PrimitivePrivatePojo pojo = EnhancedRandom.random(PrimitivePrivatePojo.class);
        pers = DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO, new File(FILENAME));
        BarbelHisto<PrimitivePrivatePojo> core = BarbelHistoBuilder.barbel()
                .withBackboneSupplier(() -> new ConcurrentIndexedCollection<PrimitivePrivatePojo>(pers))
                .build();
        core.save(pojo);
        pers = DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO, new File(FILENAME));
        core = BarbelHistoBuilder.barbel()
                .withBackboneSupplier(() -> new ConcurrentIndexedCollection<PrimitivePrivatePojo>(pers))
                .build();
        pojo.someDouble = 123d;
        core.save(pojo, BarbelHistoContext.getBarbelClock().now().plusDays(1), EffectivePeriod.INFINITE); // save changed double to persistence
        BarbelProxy effectiveIn2Days = (BarbelProxy) core
                .retrieveOne(BarbelQueries.effectiveAt(pojo.id, BarbelHistoContext.getBarbelClock().now().plusDays(2)));
        assertEquals(123d, ((PrimitivePrivatePojo) effectiveIn2Days.getTarget()).someDouble);
        // reopen to check whether change was made persistent
        pers = DiskPersistence.onPrimaryKeyInFile(VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO, new File(FILENAME));
        core = BarbelHistoBuilder.barbel()
                .withBackboneSupplier(() -> new ConcurrentIndexedCollection<PrimitivePrivatePojo>(pers))
                .build();
        effectiveIn2Days = (BarbelProxy) core
                .retrieveOne(BarbelQueries.effectiveAt(pojo.id, BarbelHistoContext.getBarbelClock().now().plusDays(2)));
        assertEquals(123d, ((PrimitivePrivatePojo) effectiveIn2Days.getTarget()).someDouble);
    }

    @Test
    public void testSave_PrimitivePrivatePojoPartialContructor() throws IOException {
        PrimitivePrivatePojoPartialContructor pojo = EnhancedRandom.random(PrimitivePrivatePojoPartialContructor.class);
        BarbelHisto<PrimitivePrivatePojoPartialContructor> core = BarbelHistoBuilder.barbel()
                .withBackboneSupplier(
                        () -> new ConcurrentIndexedCollection<PrimitivePrivatePojoPartialContructor>(DiskPersistence
                                .onPrimaryKeyInFile(VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO_PARTIAL, new File(FILENAME))))
                .build();
        core.save(pojo);
        core = BarbelHistoBuilder.barbel()
                .withBackboneSupplier(
                        () -> new ConcurrentIndexedCollection<PrimitivePrivatePojoPartialContructor>(DiskPersistence
                                .onPrimaryKeyInFile(VERSION_ID_PK_PRIMITIVE_PRIVATE_POJO_PARTIAL, new File(FILENAME))))
                .build();
        PrimitivePrivatePojoPartialContructor saved = (PrimitivePrivatePojoPartialContructor) core
                .save(pojo, BarbelHistoContext.getBarbelClock().now().plusDays(1), EffectivePeriod.INFINITE).getUpdateRequest();
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.unload(((Bitemporal) saved).getBitemporalStamp().getDocumentId());
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
    }

    @Test
    public void testSave_NoPrimitivePrivatePojoPartialContructor() throws IOException {
        NoPrimitivePrivatePojoPartialContructor pojo = EnhancedRandom
                .random(NoPrimitivePrivatePojoPartialContructor.class);
        BarbelHisto<NoPrimitivePrivatePojoPartialContructor> core = BarbelHistoBuilder.barbel().withBackboneSupplier(
                () -> new ConcurrentIndexedCollection<NoPrimitivePrivatePojoPartialContructor>(DiskPersistence
                        .onPrimaryKeyInFile(VERSION_ID_PK_NO_PRIMITIVE_PRIVATE_POJO_PARTIAL, new File(FILENAME))))
                .build();
        core.save(pojo);
        core = BarbelHistoBuilder.barbel().withBackboneSupplier(
                () -> new ConcurrentIndexedCollection<NoPrimitivePrivatePojoPartialContructor>(DiskPersistence
                        .onPrimaryKeyInFile(VERSION_ID_PK_NO_PRIMITIVE_PRIVATE_POJO_PARTIAL, new File(FILENAME))))
                .build();
        NoPrimitivePrivatePojoPartialContructor saved = (NoPrimitivePrivatePojoPartialContructor) core
                .save(pojo, BarbelHistoContext.getBarbelClock().now().plusDays(1), EffectivePeriod.INFINITE).getUpdateRequest();
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.unload(((Bitemporal) saved).getBitemporalStamp().getDocumentId());
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
    }

    @Test
    public void testSave_ComplexFieldsPrivatePojoPartialContructor() throws IOException {
        ComplexFieldsPrivatePojoPartialContructor pojo = EnhancedRandom
                .random(ComplexFieldsPrivatePojoPartialContructor.class);
        BarbelHisto<ComplexFieldsPrivatePojoPartialContructor> core = BarbelHistoBuilder.barbel().withBackboneSupplier(
                () -> new ConcurrentIndexedCollection<ComplexFieldsPrivatePojoPartialContructor>(DiskPersistence
                        .onPrimaryKeyInFile(ComplexFieldsPrivatePojoPartialContructor_Field, new File(FILENAME))))
                .build();
        core.save(pojo);
        core = BarbelHistoBuilder.barbel().withBackboneSupplier(
                () -> new ConcurrentIndexedCollection<ComplexFieldsPrivatePojoPartialContructor>(DiskPersistence
                        .onPrimaryKeyInFile(ComplexFieldsPrivatePojoPartialContructor_Field, new File(FILENAME))))
                .build();
        ComplexFieldsPrivatePojoPartialContructor saved = (ComplexFieldsPrivatePojoPartialContructor) core
                .save(pojo, BarbelHistoContext.getBarbelClock().now().plusDays(1),EffectivePeriod.INFINITE).getUpdateRequest();
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.unload(((Bitemporal) saved).getBitemporalStamp().getDocumentId());
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
    }

    @Test
    public void testSave_ComplexFieldsPrivatePojoPartialContructorWithComplexType() throws IOException {
        ComplexFieldsPrivatePojoPartialContructorWithComplexType pojo = EnhancedRandom
                .random(ComplexFieldsPrivatePojoPartialContructorWithComplexType.class);
        BarbelHisto<ComplexFieldsPrivatePojoPartialContructorWithComplexType> core = BarbelHistoBuilder.barbel()
                .withBackboneSupplier(
                        () -> new ConcurrentIndexedCollection<ComplexFieldsPrivatePojoPartialContructorWithComplexType>(
                                DiskPersistence.onPrimaryKeyInFile(
                                        ComplexFieldsPrivatePojoPartialContructorWithComplexType_Field,
                                        new File(FILENAME))))
                .build();
        core.save(pojo);
        core = BarbelHistoBuilder.barbel().withBackboneSupplier(
                () -> new ConcurrentIndexedCollection<ComplexFieldsPrivatePojoPartialContructorWithComplexType>(
                        DiskPersistence.onPrimaryKeyInFile(
                                ComplexFieldsPrivatePojoPartialContructorWithComplexType_Field, new File(FILENAME))))
                .build();
        ComplexFieldsPrivatePojoPartialContructorWithComplexType saved = (ComplexFieldsPrivatePojoPartialContructorWithComplexType) core
                .save(pojo, BarbelHistoContext.getBarbelClock().now().plusDays(1)).getUpdateRequest();
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.unload(((Bitemporal) saved).getBitemporalStamp().getDocumentId());
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
    }

}
