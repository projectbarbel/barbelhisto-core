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
import org.projectbarbel.histo.model.DefaultPojo;

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
    
    public static final SimpleAttribute<DefaultPojo, String> DOCUMENT_ID_PK = new SimpleAttribute<DefaultPojo, String>("documentId") {
        public String getValue(DefaultPojo object, QueryOptions queryOptions) {
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
    
    public static final SimpleAttribute<DefaultPojo, String> DOCUMENT_ID_PK_POJO = new SimpleAttribute<DefaultPojo, String>("documentId") {
        public String getValue(DefaultPojo object, QueryOptions queryOptions) {
            return (String)BarbelMode.POJO.drawDocumentId(object);
        }
    };
    
    public static final SimpleAttribute<DefaultPojo, String> DOCUMENT_ID_PK_POJO_PROXY = new SimpleAttribute<DefaultPojo, String>("documentId") {
        public String getValue(DefaultPojo object, QueryOptions queryOptions) {
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
        DiskPersistence<DefaultPojo, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK_POJO, new File("def.dat"));
        ConcurrentIndexedCollection<DefaultPojo> col = new ConcurrentIndexedCollection<DefaultPojo>(pers);
        col.add(new DefaultPojo("id","some"));
        assertEquals("some",((DefaultPojo)col.retrieve(equal(DOCUMENT_ID_PK_POJO, "id")).stream().findFirst().get()).getData());
        col.clear();
    }
    
    @Test
    public void pojo_update() throws IOException {
    	DiskPersistence<DefaultPojo, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK_POJO, new File("def.dat"));
    	ConcurrentIndexedCollection<DefaultPojo> col1 = new ConcurrentIndexedCollection<DefaultPojo>(pers);
    	DefaultPojo pojo = new DefaultPojo("id","some");
    	col1.add(pojo);
    	assertEquals("some",((DefaultPojo)col1.retrieve(equal(DOCUMENT_ID_PK_POJO, "id")).stream().findFirst().get()).getData());
    	pojo.setData("changed");
    	ConcurrentIndexedCollection<DefaultPojo> col2 = new ConcurrentIndexedCollection<DefaultPojo>(pers);
    	assertNotEquals("changed",((DefaultPojo)col2.retrieve(equal(DOCUMENT_ID_PK_POJO, "id")).stream().findFirst().get()).getData());
    	col2.update(Collections.singletonList(pojo), Collections.singletonList(pojo));
    	ConcurrentIndexedCollection<DefaultPojo> col3 = new ConcurrentIndexedCollection<DefaultPojo>(pers);
    	assertEquals("changed",((DefaultPojo)col3.retrieve(equal(DOCUMENT_ID_PK_POJO, "id")).stream().findFirst().get()).getData());
    }
    
    @Test
    public void pojoProxied() throws IOException {
        DiskPersistence<DefaultPojo, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK_POJO_PROXY, new File("def.dat"));
        ConcurrentIndexedCollection<DefaultPojo> col = new ConcurrentIndexedCollection<DefaultPojo>(pers);
        DefaultPojo memproxy = (DefaultPojo)BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(), new DefaultPojo("id","some"), BitemporalStamp.createActive());
        col.add(memproxy);
        DefaultPojo pojoproxy = (DefaultPojo)col.retrieve(equal(DOCUMENT_ID_PK_POJO, "id")).stream().findFirst().get();
        assertNotEquals("some",pojoproxy.getData());
        col.clear();
    }
    
}
