package com.projectbarbel.histo.persistence.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelHistoFactory.DefaultHistoType;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.persistence.api.DocumentService;

public class DefaultDocumentServiceTest {

    private DocumentService<DefaultDocument, String> service;
    private DefaultDocumentDao dao;

    @Before
    public void setUp() {
        dao = BarbelHistoContext.createDefault().factory().instanceOf(DefaultHistoType.DAO);
        service = BarbelHistoContext.createDefault().factory().instanceOf(DefaultHistoType.SERVICE, dao);
        dao.reset();
    }

    @Test
    public void testProxy_create() {
        DocumentService<DefaultDocument, String> service = DocumentService.proxy();
        assertEquals(service.save(DefaultDocument.builder().build()).get(), "not implemented");
    }

    @Test
    public void testCreateInitialDocument() {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        Optional<String> oid = service.save(object);
        assertTrue(oid.isPresent());
    }

    @Test
    public void testCreateSecondVersion() {
        DefaultDocument object = BarbelTestHelper.random(DefaultDocument.class);
        Optional<String> oid = service.save(object);
        assertTrue(oid.isPresent());
        fail("fehlen diverse tests");
    }

    @Test
    public void testSave() throws Exception {
        throw new RuntimeException("not yet implemented");
    }
    
}
