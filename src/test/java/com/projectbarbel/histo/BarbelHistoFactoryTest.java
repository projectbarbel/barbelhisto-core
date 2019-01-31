package com.projectbarbel.histo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoFactory.DefaultHistoType;
import com.projectbarbel.histo.api.VersionUpdate;
import com.projectbarbel.histo.functions.DefaultIDGenerator;
import com.projectbarbel.histo.functions.journal.KeepSubsequentUpdateStrategy;
import com.projectbarbel.histo.functions.update.DefaultPojoCopier;
import com.projectbarbel.histo.model.DefaultDocument;
import com.projectbarbel.histo.persistence.impl.DefaultDocumentDao;
import com.projectbarbel.histo.persistence.impl.DefaultDocumentService;

public class BarbelHistoFactoryTest {

    BarbelHistoFactory factory;

    @Before
    public void setUp() {
        BarbelHistoContext ctx = BarbelHistoContext.createDefault();
        factory = ctx.factory();
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

    @Test
    public void testCreate_withBarbelHistoOptions() throws Exception {
        BarbelHistoFactory factory = BarbelHistoFactory
                .create(BarbelHistoOptions.builderWithDefaultValues().withDaoClassName("blabla").build());
        assertEquals(factory.options().getDaoClassName(), "blabla");
    }

    @Test
    public void testCreate_withBarbelHistoOptionsAndFactories() throws Exception {
        BarbelHistoFactory factory = BarbelHistoFactory.create(
                BarbelHistoOptions.builderWithDefaultValues().withDaoClassName("blabla").build(),
                Collections.emptyMap());
        assertTrue(factory.factories().size() == 0);
    }

    @Test
    public void testFactories() throws Exception {
        assertTrue(BarbelHistoFactory.withDefaultValues().factories().size() > 0);
    }

    @Test
    public void testInstanceOfHistoType_DefaultDao() throws Exception {
        DefaultDocumentDao dao = factory.instanceOf(DefaultHistoType.DAO);
        assertNotNull(dao);
    }

    @Test(expected = RuntimeException.class)
    public void testInstanceOfHistoType_DefaultUpdater() throws Exception {
        VersionUpdate<DefaultDocument> bean = factory.instanceOf(DefaultHistoType.UPDATER);
        assertNotNull(bean);
    }

    @Test
    public void testInstanceOfHistoType_DefaultCopier() throws Exception {
        DefaultPojoCopier<DefaultDocument> bean = factory.instanceOf(DefaultHistoType.COPIER);
        assertNotNull(bean);
    }

    @Test
    public void testInstanceOfHistoType_IDGenerator() throws Exception {
        DefaultIDGenerator bean = factory.instanceOf(DefaultHistoType.IDGENERATOR);
        assertNotNull(bean);
    }

    @Test
    public void testInstanceOfHistoType_UpdatePolicy() throws Exception {
        KeepSubsequentUpdateStrategy<DefaultDocument> bean = factory.instanceOf(DefaultHistoType.UPDATEPOLICY);
        assertNotNull(bean);
    }
    
    @Test(expected = RuntimeException.class)
    public void testInstanceOfHistoType_Service() throws Exception {
        DefaultDocumentService bean = factory.instanceOf(DefaultHistoType.SERVICE);
        assertNotNull(bean);
    }

    @Test
    public void testInstanceOfHistoTypeObjectArray_Service() throws Exception {
        DefaultDocumentService bean = factory.instanceOf(DefaultHistoType.SERVICE, new DefaultDocumentDao());
        assertNotNull(bean);
    }

}
