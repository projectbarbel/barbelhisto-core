package org.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.equal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.projectbarbel.histo.BarbelHistoCore.DumpMode;
import org.projectbarbel.histo.model.Bitemporal;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;

import io.github.benas.randombeans.api.EnhancedRandom;

public class BarbelHistoCore_CQIndexing_Test {

    public static final SimpleAttribute<Object, String> VERSION_ID_PK = new SimpleAttribute<Object, String>(
            "documentId") {
        public String getValue(Object object, QueryOptions queryOptions) {
            return (String) ((Bitemporal)object).getBitemporalStamp().getVersionId();
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

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("createPojos")
    public <T> void testSave(T pojo) throws IOException {
        BarbelHisto<T> core = BarbelHistoBuilder.barbel()
                .withBackboneSupplier(()->{
                    IndexedCollection<T> backbone = new ConcurrentIndexedCollection<T>();
                    backbone.addIndex((Index<T>) NavigableIndex.onAttribute(VERSION_ID_PK));
                    return backbone;
                })
                .build();
        core.save(pojo, LocalDate.now(), LocalDate.MAX);
        core.save(pojo, LocalDate.now().plusDays(1), LocalDate.MAX);
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal object = (Bitemporal)core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        Bitemporal byPK = (Bitemporal)core.retrieve((Query<T>) equal(VERSION_ID_PK, (String)object.getBitemporalStamp().getVersionId())).stream().findFirst().get();
        assertNotEquals(object, byPK);
        assertEquals(object.getBitemporalStamp(), byPK.getBitemporalStamp());
        assertNotSame(object.getBitemporalStamp(), byPK.getBitemporalStamp());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.dump(DumpMode.CLEARCOLLECTION);
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
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
