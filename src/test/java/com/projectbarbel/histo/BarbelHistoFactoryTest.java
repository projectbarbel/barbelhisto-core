package com.projectbarbel.histo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoFactory.HistoType;
import com.projectbarbel.histo.dao.DocumentDao;
import com.projectbarbel.histo.model.DefaultIDGenerator;
import com.projectbarbel.histo.model.DefaultPojoCopier;
import com.projectbarbel.histo.model.DefaultValueObject;
import com.projectbarbel.histo.service.DefaultDocumentService;
import com.projectbarbel.histo.service.DocumentService;
import com.projectbarbel.histo.service.DocumentService.DocumentServiceProxy;

public class BarbelHistoFactoryTest {

    @Before
    public void setUp() {
        BarbelHistoOptions.ACTIVE_CONFIG = BarbelHistoOptions.DEFAULT_CONFIG;
        BarbelHistoFactory.initialize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstanceOfFactoryFactoryType_nullType() {
        HistoType type = null;
        BarbelHistoFactory.instanceOf(type);
    }

    @Test(expected = PassedException.class)
    public void testInstanceOfDaoFactory_withConfig_wrongName() {
        try {
            BarbelHistoFactory.instanceOf(HistoType.DAO,
                    BarbelHistoOptions.builder().withDaoClassName("bla").withIdGeneratorClassName("")
                            .withPojoCopierClassName("").withServiceClassName("").withUpdaterClassName("").build());
        } catch (RuntimeException e) {
            BarbelTestHelper.passed();
        }
        fail();
    }

    @Test()
    public void testInstanceOfDaoFactory_withCustomConfig_serviceClass() {
        BarbelHistoOptions.ACTIVE_CONFIG = BarbelHistoOptions.builder()
                .withDaoClassName("com.projectbarbel.histo.service.DocumentService$DocumentServiceProxy")
                .withIdGeneratorClassName("").withPojoCopierClassName("").withServiceClassName("")
                .withUpdaterClassName("").build();
        Object bean = BarbelHistoFactory.instanceOf(HistoType.DAO);
        assertTrue(bean instanceof DocumentServiceProxy);
    }

    @Test()
    public void testInstanceOfDaoFactory_withConfig_serviceClass_stringFactoryName() {
        BarbelHistoOptions.ACTIVE_CONFIG = BarbelHistoOptions.builder()
                .withDaoClassName("com.projectbarbel.histo.service.DocumentService$DocumentServiceProxy")
                .withIdGeneratorClassName("").withPojoCopierClassName("").withServiceClassName("")
                .withUpdaterClassName("").build();
        Object bean = BarbelHistoFactory.instanceOf("DAO");
        assertTrue(bean instanceof DocumentServiceProxy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstanceOfDaoFactory_withConfig_ConfigIsNull() {
        BarbelHistoFactory.instanceOf("DAO", (Object[])null);
    }

    @Test()
    public void testInstanceOfFactoryFactoryType_DefaultDaoFactory() {
        Object factory = BarbelHistoFactory.instanceOf(HistoType.DAO);
        assertNotNull(factory);
    }

    @Test()
    public void testInstanceOfFactoryFactoryType_DefaultDaoProduct() {
        DocumentDao<DefaultValueObject, String> dao = BarbelHistoFactory.instanceOf(HistoType.DAO);
        assertNotNull(dao);
    }

    @Test
    public void testInstanceOfFactoryFactoryType_DefaultIDGeneratror() throws Exception {
        DefaultIDGenerator generator = BarbelHistoFactory.instanceOf(HistoType.IDGENERATOR);
        assertNotNull(generator);
    }

    @Test()
    public void testInstanceOfFactoryFactoryType_DefaultServiceFactory() {
        DefaultDocumentService service = BarbelHistoFactory.instanceOf(HistoType.SERVICE, new Object[] {BarbelHistoFactory.instanceOf(HistoType.DAO)});
        assertNotNull(service);
    }

    @Test()
    @SuppressWarnings("rawtypes")
    public void testInstanceOfFactoryFactoryType_DefaultServiceFactory_callTwice_ShouldBeSameFactory() {
        DocumentService service1 = BarbelHistoFactory.instanceOf(HistoType.SERVICE, new Object[] {BarbelHistoFactory.instanceOf(HistoType.DAO)});
        DocumentService service2 = BarbelHistoFactory.instanceOf(HistoType.SERVICE, new Object[] {BarbelHistoFactory.instanceOf(HistoType.DAO)});
        assertTrue(service1 == service2);
    }

    @Test
    public void testInstanceOfFactoryType() throws Exception {
        DefaultPojoCopier copier = BarbelHistoFactory.instanceOf(HistoType.COPIER);
        assertNotNull(copier);
    }

    @Test
    public void testInstanceOfFactoryType_IDSupplier() throws Exception {
        Supplier<String> idSupplier = BarbelHistoFactory.instanceOf(HistoType.IDGENERATOR);
        assertNotNull(idSupplier);
    }

    @Test
    public void testInstantiate_withArgs() throws Exception {
        Integer four = BarbelHistoFactory.instantiate("java.lang.Integer", "4");
        assertEquals(new Integer(4), four);
    }

    @Test
    public void testInstantiate() throws Exception {
        String string = BarbelHistoFactory.instantiate("java.lang.String");
        assertEquals(new String(), string);
    }

    @Test(expected = RuntimeException.class)
    public void testInstantiate_withUnknownType() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

}
