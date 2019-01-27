package com.projectbarbel.histo.persistence.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelHistoFactory.DefaultHistoType;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.persistence.impl.DefaultDocumentDao;

public class DefaultDocumentDaoTest {

    private static DefaultDocumentDao dao;
    
    @BeforeClass
    public static void beforeClass() {
        dao = BarbelHistoContext.createDefault().factory().instanceOf(DefaultHistoType.DAO);
    }

    @Before
    public void setUp() {
        dao.reset();
    }

    @Test
    public void testCreateDocument() {
        DefaultDocument document = BarbelTestHelper.random(DefaultDocument.class);
        Optional<String> id = dao.createDocument(document);
        assertNotNull(id);
    }

    @Test(expected=RuntimeException.class)
    public void testCreateDocument_twice_shouldThrowException() {
        DefaultDocument document = BarbelTestHelper.random(DefaultDocument.class);
        dao.createDocument(document);
        dao.createDocument(document);
    }
    
    @Test(expected=RuntimeException.class)
    public void testUpdateDocument_nothingToUpdate() {
        DefaultDocument document = BarbelTestHelper.random(DefaultDocument.class);
        dao.updateDocument(document.getVersionId(), document);
    }

    @Test
    public void testDeleteDocument() {
        DefaultDocument document = BarbelTestHelper.random(DefaultDocument.class);
        dao.createDocument(document);
        Optional<DefaultDocument> object = dao.readDocument(document.getVersionId());
        assertTrue(object.isPresent());
        long deleted = dao.deleteDocument(document.getVersionId());
        assertTrue(deleted==1);
        object = dao.readDocument(document.getVersionId());
        assertFalse(object.isPresent());
    }

    @Test
    public void testDeleteDocument_noDocument() {
        DefaultDocument document = BarbelTestHelper.random(DefaultDocument.class);
        long deleted = dao.deleteDocument(document.getVersionId());
        assertTrue(deleted==0);
    }
    
    @Test
    public void testReadDocument() {
        DefaultDocument document = BarbelTestHelper.random(DefaultDocument.class);
        dao.createDocument(document);
        Optional<DefaultDocument> object = dao.readDocument(document.getVersionId());
        assertTrue(object.isPresent());
    }

    @Test
    public void testReset() {
        DefaultDocument document = BarbelTestHelper.random(DefaultDocument.class);
        dao.createDocument(document);
        Optional<DefaultDocument> object = dao.readDocument(document.getVersionId());
        assertTrue(object.isPresent());
        dao.reset();
        object = dao.readDocument(document.getVersionId());
        assertFalse(object.isPresent());
    }

    @Test
    public void testReadJournal() {
        
    }
    
}
