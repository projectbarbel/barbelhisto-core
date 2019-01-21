package com.projectbarbel.histo.dao;

import static org.junit.Assert.*;

import org.junit.Test;

import com.projectbarbel.histo.model.DefaultValueObject;

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
