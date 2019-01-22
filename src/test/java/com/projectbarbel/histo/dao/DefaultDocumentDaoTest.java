package com.projectbarbel.histo.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelHistoFactory.FactoryType;
import com.projectbarbel.histo.BarbelHistoOptions;
import com.projectbarbel.histo.model.DefaultValueObject;

import io.github.benas.randombeans.api.EnhancedRandom;

public class DefaultDocumentDaoTest {

    private static DefaultDocumentDao dao;
    
    @BeforeClass
    public static void beforeClass() {
        dao = BarbelHistoFactory.createProduct(FactoryType.DAO, BarbelHistoOptions.DEFAULT_CONFIG);
    }

    @Before
    public void setUp() {
        dao.reset();
    }

    @Test
    public void testCreateDocument() {
        DefaultValueObject document = EnhancedRandom.random(DefaultValueObject.class);
        Optional<String> id = dao.createDocument(document);
        assertNotNull(id);
    }

    @Test(expected=RuntimeException.class)
    public void testCreateDocument_twice_shouldThrowException() {
        DefaultValueObject document = EnhancedRandom.random(DefaultValueObject.class);
        dao.createDocument(document);
        dao.createDocument(document);
    }
    
    @Test(expected=RuntimeException.class)
    public void testUpdateDocument_nothingToUpdate() {
        DefaultValueObject document = EnhancedRandom.random(DefaultValueObject.class);
        dao.updateDocument(document.getVersionId(), document);
    }

    @Test
    public void testDeleteDocument() {
        DefaultValueObject document = EnhancedRandom.random(DefaultValueObject.class);
        dao.createDocument(document);
        Optional<DefaultValueObject> object = dao.readDocument(document.getVersionId());
        assertTrue(object.isPresent());
        long deleted = dao.deleteDocument(document.getVersionId());
        assertTrue(deleted==1);
        object = dao.readDocument(document.getVersionId());
        assertFalse(object.isPresent());
    }

    @Test
    public void testDeleteDocument_noDocument() {
        DefaultValueObject document = EnhancedRandom.random(DefaultValueObject.class);
        long deleted = dao.deleteDocument(document.getVersionId());
        assertTrue(deleted==0);
    }
    
    @Test
    public void testReadDocument() {
        DefaultValueObject document = EnhancedRandom.random(DefaultValueObject.class);
        dao.createDocument(document);
        Optional<DefaultValueObject> object = dao.readDocument(document.getVersionId());
        assertTrue(object.isPresent());
    }

    @Test
    public void testReset() {
        DefaultValueObject document = EnhancedRandom.random(DefaultValueObject.class);
        dao.createDocument(document);
        Optional<DefaultValueObject> object = dao.readDocument(document.getVersionId());
        assertTrue(object.isPresent());
        dao.reset();
        object = dao.readDocument(document.getVersionId());
        assertFalse(object.isPresent());
    }

    @Test
    public void testReadJournal() {
        
    }
    
}
