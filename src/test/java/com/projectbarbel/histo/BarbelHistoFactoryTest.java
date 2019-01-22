package com.projectbarbel.histo;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.function.Supplier;

import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoFactory.FactoryType;
import com.projectbarbel.histo.dao.DocumentDao;
import com.projectbarbel.histo.model.DefaultValueObject;
import com.projectbarbel.histo.service.DocumentService.DocumentServiceProxy;

public class BarbelHistoFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFactoryFactoryType_wrongType() {
        BarbelHistoFactory.createFactory("");
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDaoFactory_withConfig_wrongName() {
        BarbelHistoFactory.createFactory(FactoryType.DAO, BarbelHistoOptions.builder().withDaoSupplierClassName("bla").build());
    }

    @Test()
    public void testCreateDaoFactory_withConfig_serviceClass() {
        Supplier<?> supplier = BarbelHistoFactory.createFactory(FactoryType.DAO, BarbelHistoOptions.builder().withDaoSupplierClassName("com.projectbarbel.histo.service.DocumentService$DocumentServiceProxy").withServiceSupplierClassName("").build());
        assertTrue(supplier instanceof DocumentServiceProxy);
    }
    
    @Test()
    public void testCreateDaoFactory_withConfig_serviceClass_stringFactoryName() {
        Supplier<?> supplier = BarbelHistoFactory.createFactory("DAO", BarbelHistoOptions.builder().withDaoSupplierClassName("com.projectbarbel.histo.service.DocumentService$DocumentServiceProxy").withServiceSupplierClassName("").build());
        assertTrue(supplier instanceof DocumentServiceProxy);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateDaoFactory_withConfig_ConfigIsNull() {
        BarbelHistoFactory.createFactory("DAO", null);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCreateDaoFactory_withConfig_StringFactoryType_ConfigValueIsNull() {
        BarbelHistoFactory.createFactory("DAO", BarbelHistoOptions.builder().withDaoSupplierClassName(null).build());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCreateDaoFactory_withConfig_EnumFactoryType_ConfigValueIsNull() {
        BarbelHistoFactory.createFactory(FactoryType.DAO, BarbelHistoOptions.builder().withDaoSupplierClassName(null).build());
    }
    
    @Test()
    public void testCreateFactoryFactoryType_DefaultDaoFactory() {
        Supplier<?> factory = BarbelHistoFactory.createFactory(FactoryType.DAO);
        assertNotNull(factory);
    }

    @Test()
    public void testCreateFactoryFactoryType_DefaultDaoProduct() {
        DocumentDao<DefaultValueObject, String> dao = BarbelHistoFactory.createProduct(FactoryType.DAO);
        assertNotNull(dao);
    }
    
    @Test()
    public void testCreateFactoryFactoryTypeWithOptions_DefaultDaoProduct() {
        DocumentDao<DefaultValueObject, String> dao = BarbelHistoFactory.createProduct(FactoryType.DAO, BarbelHistoOptions.DEFAULT_CONFIG);
        assertNotNull(dao);
    }
    
    @Test()
    public void testCreateFactoryFactoryType_DefaultServiceFactory() {
        Supplier<?> factory = BarbelHistoFactory.createFactory(FactoryType.SERVICE);
        assertNotNull(factory);
    }

    @Test()
    public void testCreateFactoryFactoryType_DefaultServiceFactory_callTwice_ShouldBeSameFactory() {
        Supplier<?> factory1 = BarbelHistoFactory.createFactory(FactoryType.SERVICE);
        Supplier<?> factory2 = BarbelHistoFactory.createFactory(FactoryType.SERVICE);
        assertTrue(factory1==factory2);
    }
    
    @Test(expected = RuntimeException.class)
    public void testInstanceByClassName_UnknownClass() {
        BarbelHistoFactory.supplierBySupplierClassName("some.senseless.classpath.NonExistingClass");
    }

    @Test(expected = RuntimeException.class)
    public void testInstanceByClassName_GenerateString() {
        BarbelHistoFactory.supplierBySupplierClassName("java.jang.String");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstanceByClassName_NullValuePassed() {
        BarbelHistoFactory.supplierBySupplierClassName(null);
    }
}
