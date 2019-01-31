package com.projectbarbel.histo.persistence.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelHistoFactory.DefaultHistoType;
import com.projectbarbel.histo.model.DefaultDocument;

public class DefaultDocumentServiceTest {

    @SuppressWarnings("unused")
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

}
