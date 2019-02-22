package org.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.equal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.IOException;
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
        T saved = core.save(pojo, LocalDate.now().plusDays(1), LocalDate.MAX);
        assertEquals(3, core.retrieve(BarbelQueries.all()).stream().count());
        Bitemporal object = (Bitemporal)core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        Bitemporal byPK = (Bitemporal)core.retrieve((Query<T>) equal(VERSION_ID_PK, (String)object.getBitemporalStamp().getVersionId())).stream().findFirst().get();
        assertEquals(object, byPK);
        assertEquals(object.getBitemporalStamp(), byPK.getBitemporalStamp());
        assertNotSame(object.getBitemporalStamp(), byPK.getBitemporalStamp());
        Bitemporal record = (Bitemporal) core.retrieve(BarbelQueries.all()).stream().findFirst().get();
        assertNotNull(record.getBitemporalStamp().getDocumentId());
        core.unload(((Bitemporal)saved).getBitemporalStamp().getDocumentId());
        assertEquals(0, core.retrieve(BarbelQueries.all()).stream().count());
    }

}
