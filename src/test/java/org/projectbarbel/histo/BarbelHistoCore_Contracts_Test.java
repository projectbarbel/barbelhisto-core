package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojo;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.query.option.QueryOptions;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCore_Contracts_Test {

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get("test.dat"));
        Files.deleteIfExists(Paths.get("test.dat-wal"));
        Files.deleteIfExists(Paths.get("test.dat-shm"));
    }

    @Test
    public void testSave() throws Exception {
        BarbelHisto<String> core = BarbelHistoBuilder.barbel().build();
        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> core.save("some", LocalDate.now(), LocalDate.MAX));
        assertTrue(exc.getMessage().contains("@DocumentId"));
    }

    @Test
    public void testSaveBitemporalVersion() throws Exception {
        BarbelHisto<BitemporalVersion<DefaultPojo>> core = BarbelHistoBuilder.barbel().build();
        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> core.save(new BitemporalVersion<DefaultPojo>(BitemporalStamp.createActive(), EnhancedRandom.random(DefaultPojo.class)), LocalDate.now(), LocalDate.MAX));
        assertTrue(exc.getMessage().contains("BitemporalVersion cannot be used in BarbelMode.POJO"));
    }
    
    @Test
    public void testSave_LocalDatesInvalid() throws Exception {
        BarbelHisto<DefaultPojo> core = BarbelHistoBuilder.barbel().build();
        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> core.save(EnhancedRandom.random(DefaultPojo.class), LocalDate.MAX, LocalDate.now()));
        assertTrue(exc.getMessage().contains("from date"));
    }

    @Test
    public void testSave_NoSerializer() throws Exception {
        BarbelHisto<SomePojo> core = BarbelHistoBuilder.barbel()
                .withBackboneSupplier(() -> new ConcurrentIndexedCollection<SomePojo>(
                        DiskPersistence.onPrimaryKeyInFile(SomePojo.DOCUMENT_ID, new File("test.dat"))))
                .build();
        SomePojo pojo = EnhancedRandom.random(SomePojo.class);
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        Exception exc = assertThrows(RuntimeException.class,
                () -> core.retrieve(BarbelQueries.all()));
        assertTrue(exc.getMessage().contains("@PersistenceConfig"));
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
    
    @ParameterizedTest
    @MethodSource("nullableParameters")
    public <T> void testSaveParameter(T pojo, LocalDate from, LocalDate until) {
        BarbelHisto<T> core = BarbelHistoBuilder.barbel().build();
        assertThrows(IllegalArgumentException.class, ()->core.save(pojo, from, until));
    }
    
    public static class SomePojo {
        public static final SimpleAttribute<SomePojo, String> DOCUMENT_ID = new SimpleAttribute<SomePojo, String>("documentId") {
            public String getValue(SomePojo object, QueryOptions queryOptions) { return object.getId(); }
        };

        @DocumentId
        private String id;
        private String some;
        public String getId() {
            return id;
        }
        public String getSome() {
            return some;
        }
        public void setId(String id) {
            this.id = id;
        }
        public void setSome(String some) {
            this.some = some;
        }
    }

}
