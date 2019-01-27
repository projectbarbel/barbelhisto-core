package com.projectbarbel.histo.persistence.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.persistence.api.DocumentDao;

public class DocumentDaoTest {

    @Test
    public void testProxy_create() {
        DocumentDao<DefaultDocument, String> dao = DocumentDao.proxy();
        assertEquals(dao.createDocument(DefaultDocument.builder().build()).get(), "not implemented");
    }

    @Test
    public void testProxy_delete() {
        DocumentDao<DefaultDocument, String> dao = DocumentDao.proxy();
        assertTrue(dao.deleteDocument("blas")==42);
    }
    
}
