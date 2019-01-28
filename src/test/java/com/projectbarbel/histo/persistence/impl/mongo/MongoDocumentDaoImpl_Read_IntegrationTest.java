package com.projectbarbel.histo.persistence.impl.mongo;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelHistoFactory.DefaultHistoType;
import com.projectbarbel.histo.BarbelHistoOptions;
import com.projectbarbel.histo.BarbelTestHelper;

public class MongoDocumentDaoImpl_Read_IntegrationTest {

    private static MongoDocumentDaoImpl dao;

    @BeforeClass
    public static void beforeClass() {
        BarbelHistoOptions opts = BarbelHistoOptions.withDaoClassName("com.projectbarbel.histo.persistence.impl.mongo.MongoDocumentDaoImpl");
        BarbelHistoContext ctx = BarbelHistoContext.of(opts);
        dao = ctx.factory().instanceOf(DefaultHistoType.DAO, new Object[] {FlapDoodleEmbeddedMongoClientDaoSupplier.MONGOCLIENT.getMongo(), "test", "testCol"});
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
