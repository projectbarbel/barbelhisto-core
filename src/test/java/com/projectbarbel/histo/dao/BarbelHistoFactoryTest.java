package com.projectbarbel.histo.dao;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelHistoFactory.FactoryType;
import com.projectbarbel.histo.BarbelHistoOptions;

public class BarbelHistoFactoryTest {

    public void testCreateFactoryFactoryType_notNull() {
        BarbelHistoFactory<DocumentDao<?,?>> factory = BarbelHistoFactory.createFactory(FactoryType.DAO);
        assertNotNull(factory);
    }

    @Test()
    public void testCreateFactoryFactoryType_DefaultDaoFactory() {
        BarbelHistoFactory<DocumentDao<?,?>> factory = BarbelHistoFactory.createFactory(FactoryType.DAO);
        assertNotNull(factory);
    }
    
    @Test()
    public void testCreateFactoryFactoryType_DefaultServiceFactory() {
        BarbelHistoFactory<DocumentDao<?,?>> factory = BarbelHistoFactory.createFactory(FactoryType.SERVICE);
        assertNotNull(factory);
    }

    @Test(expected=RuntimeException.class)
    public void testInstanceByClassName_UnknownClass() {
        BarbelHistoFactory.supplierInstanceByClassName("some.senseless.classpath.NonExistingClass", BarbelHistoOptions.DEFAULT_CONFIG);
    }
    
    @Test(expected=RuntimeException.class)
    public void testInstanceByClassName_GenerateString() {
        BarbelHistoOptions.DEFAULT_CONFIG.addOption("string", "java.lang.String");
        BarbelHistoFactory.supplierInstanceByClassName("string", BarbelHistoOptions.DEFAULT_CONFIG);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInstanceByClassName_NullValuePassed() {
        BarbelHistoFactory.supplierInstanceByClassName(null, BarbelHistoOptions.DEFAULT_CONFIG);
    }
}
