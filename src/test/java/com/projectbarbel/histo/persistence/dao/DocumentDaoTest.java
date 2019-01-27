package com.projectbarbel.histo.persistence.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.projectbarbel.histo.model.DefaultValueObject;
import com.projectbarbel.histo.persistence.dao.DocumentDao;

public class DocumentDaoTest {

    @Test
    public void testProxy_create() {
        DocumentDao<DefaultValueObject, String> dao = DocumentDao.proxy();
        assertEquals(dao.createDocument(DefaultValueObject.builder().build()).get(), "not implemented");
    }

    @Test
    public void testProxy_delete() {
        DocumentDao<DefaultValueObject, String> dao = DocumentDao.proxy();
        assertTrue(dao.deleteDocument("blas")==42);
    }
    
}
