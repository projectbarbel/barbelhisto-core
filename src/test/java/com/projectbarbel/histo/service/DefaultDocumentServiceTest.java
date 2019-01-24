package com.projectbarbel.histo.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelHistoFactory.FactoryType;
import com.projectbarbel.histo.BarbelHistoOptions;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.dao.DefaultDocumentDao;
import com.projectbarbel.histo.model.DefaultValueObject;

public class DefaultDocumentServiceTest {

    private DocumentService<DefaultValueObject, String> service;
    private DefaultDocumentDao dao;
    private static BarbelHistoOptions opts = BarbelHistoOptions.builder()
            .withDaoSupplierClassName("com.projectbarbel.histo.dao.DefaultDocumentDao$DefaultDaoSupplier")
            .withServiceSupplierClassName(
                    "com.projectbarbel.histo.service.DefaultDocumentService$DefaultDocumentServiceSupplier")
            .build();

    @Before
    public void setUp() {
        service = BarbelHistoFactory.createProduct(FactoryType.SERVICE, opts);
        dao = BarbelHistoFactory.createProduct(FactoryType.DAO, opts);
        dao.reset();
    }

    @Test
    public void testProxy_create() {
        DocumentService<DefaultValueObject, String> service = DocumentService.proxy();
        assertEquals(service.save(DefaultValueObject.builder().build()).get(), "not implemented");
    }

    @Test
    public void testCreateInitialDocument() {
        DefaultValueObject object = BarbelTestHelper.random(DefaultValueObject.class);
        Optional<String> oid = service.save(object);
        assertTrue(oid.isPresent());
    }

    @Test
    public void testCreateSecondVersion() {
        DefaultValueObject object = BarbelTestHelper.random(DefaultValueObject.class);
        Optional<String> oid = service.save(object);
        assertTrue(oid.isPresent());
        fail("fehlen diverse tests");
    }

    @Test
    public void testSave() throws Exception {
        throw new RuntimeException("not yet implemented");
    }
    
}
