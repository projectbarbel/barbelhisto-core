package com.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.equal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.functions.BarbelProxy;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.model.DefaultPojo;

public class CQEnginePersistenceTest {
    
    public static final SimpleAttribute<Object, String> DOCUMENT_ID_PK = new SimpleAttribute<Object, String>("documentId") {
        public String getValue(Object object, QueryOptions queryOptions) {
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
    public void object() throws IOException {
        DiskPersistence<Object, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK, new File("def1.dat"));
        ConcurrentIndexedCollection<Object> col = new ConcurrentIndexedCollection<Object>(pers);
        col.add(DefaultDocument.builder().withBitemporalStamp(BitemporalStamp.createActive()).withData("some").build());
        assertThrows(ClassCastException.class,()-> ((DefaultDocument)col.retrieve(BarbelQueries.all()).stream().findFirst().get()).getData());
        col.clear();
        Files.delete(Paths.get("def1.dat"));
        Files.delete(Paths.get("def1.dat-shm"));
        Files.delete(Paths.get("def1.dat-wal"));
    }

    @Test
    public void bitemporal() throws IOException {
        DiskPersistence<Bitemporal, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK_BITEMPORAL, new File("def2.dat"));
        ConcurrentIndexedCollection<Bitemporal> col = new ConcurrentIndexedCollection<Bitemporal>(pers);
        col.add(DefaultDocument.builder().withBitemporalStamp(BitemporalStamp.createActive()).withData("some").build());
        assertEquals("some",((DefaultDocument)col.retrieve(BarbelQueries.all()).stream().findFirst().get()).getData());
        col = new ConcurrentIndexedCollection<Bitemporal>(pers);
        assertEquals(1, col.size());
        col.clear();
        col = new ConcurrentIndexedCollection<Bitemporal>(pers);
        assertEquals(0, col.size());
        Files.delete(Paths.get("def2.dat"));
        Files.delete(Paths.get("def2.dat-shm"));
        Files.delete(Paths.get("def2.dat-wal"));
    }
    
    @Test
    public void concrete() throws IOException {
        DiskPersistence<DefaultDocument, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK_CONCRETE, new File("def2.dat"));
        ConcurrentIndexedCollection<DefaultDocument> col = new ConcurrentIndexedCollection<DefaultDocument>(pers);
        col.add(DefaultDocument.builder().withBitemporalStamp(BitemporalStamp.createActive()).withData("some").build());
        assertEquals("some",((DefaultDocument)col.retrieve(BarbelQueries.all()).stream().findFirst().get()).getData());
        col = new ConcurrentIndexedCollection<DefaultDocument>(pers);
        assertEquals(1, col.size());
        col.clear();
        col = new ConcurrentIndexedCollection<DefaultDocument>(pers);
        assertEquals(0, col.size());
        Files.delete(Paths.get("def2.dat"));
        Files.delete(Paths.get("def2.dat-shm"));
        Files.delete(Paths.get("def2.dat-wal"));
    }
    
    @Test
    public void pojo() throws IOException {
        DiskPersistence<DefaultPojo, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK_POJO, new File("def3.dat"));
        ConcurrentIndexedCollection<DefaultPojo> col = new ConcurrentIndexedCollection<DefaultPojo>(pers);
        col.add(new DefaultPojo("id","some"));
        assertEquals("some",((DefaultPojo)col.retrieve(equal(DOCUMENT_ID_PK_POJO, "id")).stream().findFirst().get()).getData());
        col.clear();
        Files.delete(Paths.get("def3.dat"));
        Files.delete(Paths.get("def3.dat-shm"));
        Files.delete(Paths.get("def3.dat-wal"));
    }
    
    @Test
    public void pojoProxied() throws IOException {
        DiskPersistence<DefaultPojo, String> pers = DiskPersistence.onPrimaryKeyInFile(DOCUMENT_ID_PK_POJO_PROXY, new File("def4.dat"));
        ConcurrentIndexedCollection<DefaultPojo> col = new ConcurrentIndexedCollection<DefaultPojo>(pers);
        DefaultPojo memproxy = (DefaultPojo)BarbelMode.POJO.snapshotMaiden(BarbelHistoBuilder.barbel(), new DefaultPojo("id","some"), BitemporalStamp.createActive());
        col.add(memproxy);
        DefaultPojo pojoproxy = (DefaultPojo)col.retrieve(equal(DOCUMENT_ID_PK_POJO, "id")).stream().findFirst().get();
        assertNotEquals("some",pojoproxy.getData());
        col.clear();
        Files.delete(Paths.get("def4.dat"));
        Files.delete(Paths.get("def4.dat-shm"));
        Files.delete(Paths.get("def4.dat-wal"));
    }
    
}
