package org.projectbarbel.histo.suite.standard;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.DocumentId;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojo;
import org.projectbarbel.histo.suite.BTExecutionContext;
import org.projectbarbel.histo.suite.extensions.BTTestStandard;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.query.option.QueryOptions;

import io.github.benas.randombeans.api.EnhancedRandom;

@ExtendWith(BTTestStandard.class)
public class BarbelHistoCore_Contracts_SuiteTest {

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
                () -> core.save("some"));
        assertTrue(exc.getMessage().contains("@DocumentId"));
    }

    @Test
    public void testSaveBitemporalVersion() throws Exception {
        BarbelHisto<BitemporalVersion> core = BTExecutionContext.INSTANCE.barbel(BitemporalVersion.class).build();
        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> core.save(new BitemporalVersion(BitemporalStamp.createActive(), EnhancedRandom.random(DefaultPojo.class))));
        assertTrue(exc.getMessage().contains("BitemporalVersion cannot be used in BarbelMode.POJO"));
    }
    
    @Test
    public void testSaveBitemporalInModePOJO() throws Exception {
        BarbelHisto<DefaultDocument> core = BTExecutionContext.INSTANCE.barbel(DefaultDocument.class).build();
        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> core.save(new DefaultDocument("some", "bitemporal")));
        assertTrue(exc.getMessage().contains("don't use Bitemporal.class"));
    }
    
    @Test
    public void testSave_LocalDatesInvalid() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).build();
        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> core.save(EnhancedRandom.random(DefaultPojo.class),EffectivePeriod.INFINITE,ZonedDateTime.now()));
        assertTrue(exc.getMessage().contains("from date"));
    }

    @Test
    public void testSavePojoInBitemporal() throws Exception {
        BarbelHisto<DefaultPojo> core = BTExecutionContext.INSTANCE.barbel(DefaultPojo.class).withMode(BarbelMode.BITEMPORAL).build();
        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> core.save(EnhancedRandom.random(DefaultPojo.class)));
        assertTrue(exc.getMessage().contains("don't forget"));
    }
    
    
    @Test
    public void testSave_NoSerializer() throws Exception {
        BarbelHisto<SomePojo> core = BTExecutionContext.INSTANCE.barbel(SomePojo.class)
                .withBackboneSupplier(() -> new ConcurrentIndexedCollection<SomePojo>(
                        DiskPersistence.onPrimaryKeyInFile(SomePojo.DOCUMENT_ID, new File("test.dat"))))
                .build();
        SomePojo pojo = EnhancedRandom.random(SomePojo.class);
        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> core.save(pojo));
        assertTrue(exc.getMessage().contains("@PersistenceConfig"));
    }
    
    
    @SuppressWarnings("unused")
    private static Stream<Arguments> nullableParameters() {
        return Stream.of(
                Arguments.of(EnhancedRandom.random(PrimitivePrivatePojo.class), ZonedDateTime.now(), null),
                Arguments.of(EnhancedRandom.random(PrimitivePrivatePojo.class), null, ZonedDateTime.now()),
                Arguments.of(null, ZonedDateTime.now(), EffectivePeriod.INFINITE),
                Arguments.of(new PrimitivePrivatePojo(), ZonedDateTime.now(), EffectivePeriod.INFINITE)
                );
    }
    
    @ParameterizedTest
    @MethodSource("nullableParameters")
    public <T> void testSaveParameter(T pojo, ZonedDateTime from, ZonedDateTime until) {
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
