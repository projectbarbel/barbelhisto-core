package com.projectbarbel.histo.persistence.impl.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelHistoFactory.DefaultHistoType;
import com.projectbarbel.histo.persistence.impl.mongo.DefaultMongoValueObject;
import com.projectbarbel.histo.persistence.impl.mongo.MongoDocumentDaoImpl;
import com.projectbarbel.histo.BarbelHistoOptions;
import com.projectbarbel.histo.BarbelTestHelper;

public class MongoDocumentDaoImpl_Update_IntegrationTest {

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

	@Test(expected = NullPointerException.class)
	public void testCreateDocument_null() {
		dao.createDocument(null);
	}

	@Test
	public void testUpdateDocument_updateData() {
	    DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
		Optional<ObjectId> oid = dao.createDocument(object);
		DefaultMongoValueObject doc = dao.readDocument(oid.get()).get();
		DefaultMongoValueObject changedObj = new DefaultMongoValueObject(object.getVersionId(), object.getBitemporalStamp(), "new data");
		Optional<ObjectId> newId = dao.updateDocument(oid.orElse(new ObjectId()), changedObj);
		DefaultMongoValueObject updatedObj = dao.readDocument(newId.orElse(new ObjectId())).get();
		assertEquals(changedObj, updatedObj);
		assertTrue(updatedObj.getData().equals("new data"));
		assertNotEquals(updatedObj, doc);
	}

	@Test
	public void testUpdateDocument_noUpdates() {
	    DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
	    Optional<ObjectId> oid = dao.createDocument(object);
		Optional<ObjectId> updatedOid = dao.updateDocument(oid.orElse(new ObjectId()), object);
		DefaultMongoValueObject updatedDoc = dao.readDocument(updatedOid.orElse(new ObjectId())).get();
		assertEquals(updatedDoc.getData(), object.getData());
		assertEquals(updatedDoc, object);
	}

}
