package com.projectbarbel.histo.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.function.Supplier;

import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelHistoFactory.FactoryType;
import com.projectbarbel.histo.BarbelHistoOptions;
import com.projectbarbel.histo.service.DocumentService.DocumentServiceProxy;

public class BarbelHistoFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFactoryFactoryType_wrongType() {
        BarbelHistoFactory.createFactory("");
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDaoFactory_withConfig_wrongName() {
        BarbelHistoFactory.createFactory(FactoryType.DAO, BarbelHistoOptions.builder().withDaoClassName("bla").build());
    }

    @Test()
    public void testCreateDaoFactory_withConfig_serviceClass() {
        Supplier<?> supplier = BarbelHistoFactory.createFactory(FactoryType.DAO, BarbelHistoOptions.builder().withDaoClassName("com.projectbarbel.histo.service.DocumentService$DocumentServiceProxy").withServiceClassName("").build());
        assertTrue(supplier instanceof DocumentServiceProxy);
    }
    
    @Test()
    public void testCreateDaoFactory_withConfig_serviceClass_stringFactoryName() {
        Supplier<?> supplier = BarbelHistoFactory.createFactory("DAO", BarbelHistoOptions.builder().withDaoClassName("com.projectbarbel.histo.service.DocumentService$DocumentServiceProxy").withServiceClassName("").build());
        assertTrue(supplier instanceof DocumentServiceProxy);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateDaoFactory_withConfig_ConfigIsNull() {
        BarbelHistoFactory.createFactory("DAO", null);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCreateDaoFactory_withConfig_StringFactoryType_ConfigValueIsNull() {
        BarbelHistoFactory.createFactory("DAO", BarbelHistoOptions.builder().withDaoClassName(null).build());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCreateDaoFactory_withConfig_EnumFactoryType_ConfigValueIsNull() {
        BarbelHistoFactory.createFactory(FactoryType.DAO, BarbelHistoOptions.builder().withDaoClassName(null).build());
    }
    
    @Test()
    public void testCreateFactoryFactoryType_DefaultDaoFactory() {
        Supplier<?> factory = BarbelHistoFactory.createFactory(FactoryType.DAO);
        assertNotNull(factory);
    }

    @Test()
    public void testCreateFactoryFactoryType_DefaultServiceFactory() {
        Supplier<?> factory = BarbelHistoFactory.createFactory(FactoryType.SERVICE);
        assertNotNull(factory);
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
