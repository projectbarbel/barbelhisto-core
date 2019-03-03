package org.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.equal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.DefaultDocument;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojoNoPersistence;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.query.option.QueryOptions;

public class CQEnginePersistenceTest {

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get("def.dat"));
        Files.deleteIfExists(Paths.get("def.dat-shm"));
        Files.deleteIfExists(Paths.get("def.dat-wal"));
    }
    
    public static final SimpleAttribute<PrimitivePrivatePojoNoPersistence, String> DOCUMENT_ID_PK = new SimpleAttribute<PrimitivePrivatePojoNoPersistence, String>("documentId") {
        public String getValue(PrimitivePrivatePojoNoPersistence object, QueryOptions queryOptions) {
            return (String)((Bitemporal) object).getBitemporalStamp().getDocumentId();
        }
    };

    public static final SimpleAttribute<DefaultDocument, String> DOCUMENT_ID_PK_CONCRETE = new SimpleAttribute<DefaultDocument, String>("documentId") {
        public String getValue(DefaultDocument object, QueryOptions queryOptions) {
            return (String)((Bitemporal) object).getBitemporalStamp().getDocumentId();
        }
    };
    
    public static final SimpleAttribute<Bitemporal, String> DOCUMENT_ID_PK_BITEMPORAL = new SimpleAttribute<Bitemporal, String>("documentId") {
        public String getValue(Bitemporal object, QueryOptions queryOptions) {
            return (String)((Bitemporal) object).getBitemporalStamp().getDocumentId();
        }
    };
    
    public static final SimpleAttribute<PrimitivePrivatePojoNoPersistence, String> DOCUMENT_ID_PK_POJO = new SimpleAttribute<PrimitivePrivatePojoNoPersistence, String>("documentId") {
        public String getValue(PrimitivePrivatePojoNoPersistence object, QueryOptions queryOptions) {
            return (String)BarbelMode.POJO.drawDocumentId(object);
        }
    };
    
    public static final SimpleAttribute<PrimitivePrivatePojoNoPersistence, String> DOCUMENT_ID_PK_POJO_PROXY = new SimpleAttribute<PrimitivePrivatePojoNoPersistence, String>("documentId") {
        public String getValue(PrimitivePrivatePojoNoPersistence object, QueryOptions queryOptions) {
            return (String)BarbelMode.POJO.drawDocumentId(((BarbelProxy)object).getTarget());
        }
    };
    
    @Test
    public void bitemporal() throws IOException {
        DiskPersistence<Bitemporal, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK_BITEMPORAL, new File("def.dat"));
        final ConcurrentIndexedCollection<Bitemporal> col = new ConcurrentIndexedCollection<Bitemporal>(pers);
        col.add(DefaultDocument.builder().withBitemporalStamp(BitemporalStamp.createActive()).withData("some").build());
        assertThrows(InstantiationError.class, ()->((DefaultDocument)col.retrieve(BarbelQueries.all()).stream().findFirst().get()).getData());
    }
    
    @Test
    public void concrete() throws IOException {
        DiskPersistence<DefaultDocument, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK_CONCRETE, new File("def.dat"));
        ConcurrentIndexedCollection<DefaultDocument> col = new ConcurrentIndexedCollection<DefaultDocument>(pers);
        col.add(DefaultDocument.builder().withBitemporalStamp(BitemporalStamp.createActive()).withData("some").build());
        assertEquals("some",((DefaultDocument)col.retrieve(BarbelQueries.all()).stream().findFirst().get()).getData());
        col = new ConcurrentIndexedCollection<DefaultDocument>(pers);
        assertEquals(1, col.size());
        col.clear();
        col = new ConcurrentIndexedCollection<DefaultDocument>(pers);
        assertEquals(0, col.size());
    }
    
    @Test
    public void pojo() throws IOException {
        DiskPersistence<PrimitivePrivatePojoNoPersistence, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK_POJO, new File("def.dat"));
        ConcurrentIndexedCollection<PrimitivePrivatePojoNoPersistence> col = new ConcurrentIndexedCollection<PrimitivePrivatePojoNoPersistence>(pers);
        col.add(new PrimitivePrivatePojoNoPersistence("someId"));
        assertEquals("someId",((PrimitivePrivatePojoNoPersistence)col.retrieve(equal(DOCUMENT_ID_PK_POJO, "someId")).stream().findFirst().get()).id);
        col.clear();
    }
    
    @Test
    public void pojo_update() throws IOException {
    	DiskPersistence<PrimitivePrivatePojoNoPersistence, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK_POJO, new File("def.dat"));
    	ConcurrentIndexedCollection<PrimitivePrivatePojoNoPersistence> col1 = new ConcurrentIndexedCollection<PrimitivePrivatePojoNoPersistence>(pers);
    	PrimitivePrivatePojoNoPersistence pojo = new PrimitivePrivatePojoNoPersistence("id","some");
    	col1.add(pojo);
    	assertEquals("some",((PrimitivePrivatePojoNoPersistence)col1.retrieve(equal(DOCUMENT_ID_PK_POJO, "id")).stream().findFirst().get()).getData());
    	pojo.setData("changed");
    	ConcurrentIndexedCollection<PrimitivePrivatePojoNoPersistence> col2 = new ConcurrentIndexedCollection<PrimitivePrivatePojoNoPersistence>(pers);
    	assertNotEquals("changed",((PrimitivePrivatePojoNoPersistence)col2.retrieve(equal(DOCUMENT_ID_PK_POJO, "id")).stream().findFirst().get()).getData());
    	col2.update(Collections.singletonList(pojo), Collections.singletonList(pojo));
    	ConcurrentIndexedCollection<PrimitivePrivatePojoNoPersistence> col3 = new ConcurrentIndexedCollection<PrimitivePrivatePojoNoPersistence>(pers);
    	assertEquals("changed",((PrimitivePrivatePojoNoPersistence)col3.retrieve(equal(DOCUMENT_ID_PK_POJO, "id")).stream().findFirst().get()).getData());
    }
    
    @Test
    public void pojoProxied() throws IOException {
        DiskPersistence<PrimitivePrivatePojoNoPersistence, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK_POJO_PROXY, new File("def.dat"));
        ConcurrentIndexedCollection<PrimitivePrivatePojoNoPersistence> col = new ConcurrentIndexedCollection<PrimitivePrivatePojoNoPersistence>(pers);
        PrimitivePrivatePojoNoPersistence memproxy = (PrimitivePrivatePojoNoPersistence)BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(), new PrimitivePrivatePojoNoPersistence("id","some"), BitemporalStamp.createActive());
        col.add(memproxy);
        PrimitivePrivatePojoNoPersistence pojoproxy = (PrimitivePrivatePojoNoPersistence)col.retrieve(equal(DOCUMENT_ID_PK_POJO, "id")).stream().findFirst().get();
        assertNotEquals("some",pojoproxy.getData());
        col.clear();
    }
    
}
