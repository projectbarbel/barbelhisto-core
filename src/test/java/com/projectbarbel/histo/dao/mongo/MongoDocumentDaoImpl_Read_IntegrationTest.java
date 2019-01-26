package com.projectbarbel.histo.dao.mongo;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelHistoFactory.HistoType;
import com.projectbarbel.histo.BarbelHistoOptions;
import com.projectbarbel.histo.BarbelTestHelper;

public class MongoDocumentDaoImpl_Read_IntegrationTest {

    private static MongoDocumentDaoImpl dao;

    @BeforeClass
    public static void beforeClass() {
        BarbelHistoFactory.initialize();
        BarbelHistoOptions.ACTIVE_CONFIG = BarbelHistoOptions.builder().withDefaultValues().withDaoClassName("com.projectbarbel.histo.dao.mongo.MongoDocumentDaoImpl").build();
        dao = BarbelHistoFactory.instanceOf(HistoType.DAO, new Object[] {FlapDoodleEmbeddedMongoClientDaoSupplier.MONGOCLIENT.getMongo(), "test", "testCol"});
    }


    @Before
    public void setUp() {
        dao.reset();
    }

    @Test
	public void testReadDocument() {
		DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
		dao.createDocument(object);
		assertNotNull(dao.readDocument(object.getObjectId()).get());
    }

}
